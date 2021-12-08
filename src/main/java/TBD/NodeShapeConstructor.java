package TBD;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;

import java.util.*;

public class NodeShapeConstructor {

    private final IRI enumerationPredicate = Values.iri("http://www.w3.org/ns/shacl#in");
    private final IRI commentPredicate = Values.iri("http://www.w3.org/2000/01/rdf-schema#comment");
    private final IRI labelPredicate = Values.iri("http://www.w3.org/2000/01/rdf-schema#label");
    private final Set<IRI> nodes;
    private final Set<Resource> propertyNodes;
    private final List<NodeShape> nodeShapeList;

    public NodeShapeConstructor(List<Model> constraints, List<Model> vocabularies) {
        nodes = extractUniqueNodeShapes(constraints);
        propertyNodes = extractUniquePropertyNodes(constraints);
        nodeShapeList = constructNodeShapes(constraints, vocabularies);
    }

    private Set<IRI> extractUniqueNodeShapes(List<Model> constraints) {
        IRI predicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI object = Values.iri("http://www.w3.org/ns/shacl#NodeShape");
        Set<IRI> result = new LinkedHashSet<>();

        constraints.stream()
                .flatMap(Collection::stream)
                .filter(st -> st.getPredicate().equals(predicate))
                .filter(st -> st.getObject().equals(object))
                .map(st -> (IRI) st.getSubject())
                .forEach(result::add);
        return result;
    }

    private Set<Resource> extractUniquePropertyNodes(List<Model> constraints) {
        IRI predicate = Values.iri("http://www.w3.org/ns/shacl#property");
        Set<Resource> result = new LinkedHashSet<>();

        this.nodes.forEach(node ->
                constraints.stream()
                        .flatMap(Collection::stream)
                        .filter(st -> st.getSubject().equals(node))
                        .filter(st -> st.getPredicate().equals(predicate))
                        .map(st -> (Resource) st.getObject())
                        .forEach(result::add));
        return result;
    }

    private String extractTargetClass(List<Model> constraints, IRI node) {
        IRI targetClassPredicate = Values.iri("http://www.w3.org/ns/shacl#targetClass");
        return matchOnlyOne(constraints, node, targetClassPredicate);
    }

    private List<NodeShape> constructNodeShapes(List<Model> constraints, List<Model> vocabularies) {
        List<NodeShape> nodeShapeList = new ArrayList<>();

        this.nodes.forEach(node -> {
            List<Property> propertyList = new ArrayList<>();
            List<String> nodeEnumeration = new ArrayList<>();
            this.propertyNodes.forEach(pNode ->
                    constraints.stream()
                            .flatMap(Collection::stream)
                            .filter(st -> st.getSubject().equals(node))
                            .filter(st -> st.getObject().equals(pNode))
                            .forEach(st -> propertyList.add(extractPropertyNodeAttributes(pNode, constraints, vocabularies))));

            nodeShapeEnumeration(constraints, node, nodeEnumeration);
            String targetClass = extractTargetClass(constraints, node);
            nodeShapeList.add(NodeShape.builder()
                    .nodeShapeID(extractNameFromIRI(node.stringValue()))
                    .targetClass(extractNameFromIRI(targetClass))
                    .rootObject(isRootObject(constraints, node))
                    .enumeration(nodeEnumeration)
                    .doc(extractDoc(targetClass, vocabularies))
                    .aliases(extractAliases(targetClass, vocabularies))
                    .propertyList(propertyList)
                    .build());
        });

        return nodeShapeList;
    }

    private Boolean isRootObject(List<Model> constraints, IRI node) {
        IRI rootObjectPredicate = Values.iri("http://www.w3.org/2000/01/rdf-schema#comment");
        return Objects.equals(matchOnlyOne(constraints, node, rootObjectPredicate), "RootObject");
    }

    private String matchOnlyOne(List<Model> constraints, IRI node, IRI predicate) {
        final String[] match = {null};
        constraints.stream()
                .flatMap(Collection::stream)
                .filter(st -> st.getSubject().equals(node))
                .filter(st -> st.getPredicate().equals(predicate))
                .forEach(st -> match[0] = st.getObject().stringValue());
        return match[0];
    }

    private void nodeShapeEnumeration(List<Model> constraints, IRI node, List<String> nodeEnumeration) {
        constraints.stream()
                .flatMap(Collection::stream)
                .filter(st -> st.getSubject().equals(node))
                .filter(st -> st.getPredicate().equals(enumerationPredicate))
                .forEach(st -> enumerationToList((Resource) st.getObject(), constraints, nodeEnumeration));
    }

    private Property extractPropertyNodeAttributes(Resource pNode, List<Model> constraints, List<Model> vocabularies) {
        IRI pathPredicate = Values.iri("http://www.w3.org/ns/shacl#path");
        IRI nodePredicate = Values.iri("http://www.w3.org/ns/shacl#node");
        IRI minCountPredicate = Values.iri("http://www.w3.org/ns/shacl#minCount");
        IRI maxCountPredicate = Values.iri("http://www.w3.org/ns/shacl#maxCount");
        IRI datatypePredicate = Values.iri("http://www.w3.org/ns/shacl#datatype");

        String doc;
        String path = null;
        String node = null;
        String datatype = null;
        String minCount = "0";
        String maxCount = "*";
        List<String> aliases;
        List<String> enumerationList = new ArrayList<>();

        for (Model constraint : constraints) {
            Model pNodeInfo = constraint.filter(pNode, null, null);
            for (Statement st : pNodeInfo) {
                if (st.getPredicate().equals(pathPredicate)) {
                    path = st.getObject().stringValue();
                }
                if (st.getPredicate().equals(nodePredicate)) {
                    node = st.getObject().stringValue();
                }
                if (st.getPredicate().equals(datatypePredicate)) {
                    datatype = st.getObject().stringValue();
                }
                if (st.getPredicate().equals(minCountPredicate)) {
                    minCount = st.getObject().stringValue();
                }
                if (st.getPredicate().equals(maxCountPredicate)) {
                    maxCount = st.getObject().stringValue();
                }
                if (st.getPredicate().equals(enumerationPredicate)) {
                    enumerationToList((Resource) st.getObject(), constraints, enumerationList);
                }
            }
        }
        aliases = extractAliases(path, vocabularies);
        doc = extractDoc(path, vocabularies);

        return constructPropertyShape(path, node, datatype, minCount, maxCount, doc, aliases, enumerationList);
    }

    private List<String> extractAliases(String identifier, List<Model> vocabularies) {
        List<String> aliases = new ArrayList<>();
        if (identifier != null) {
            for (Model vocabulary : vocabularies) {
                Model pNodeInfo = vocabulary.filter(Values.iri(identifier), null, null);
                for (Statement st : pNodeInfo) {
                    if (st.getPredicate().equals(labelPredicate)) {
                        aliases.add(st.getObject().stringValue());
                    }
                }
            }
        }
        return aliases;
    }

    private String extractDoc(String identifier, List<Model> vocabularies) {
        String doc = null;
        if (identifier != null) {
            for (Model vocabulary : vocabularies) {
                Model pNodeInfo = vocabulary.filter(Values.iri(identifier), null, null);
                for (Statement st : pNodeInfo) {
                    if (st.getPredicate().equals(commentPredicate)) {
                        doc = st.getObject().stringValue();
                    }
                }
            }
        }
        return doc;
    }

    private void enumerationToList(Resource firstNode, List<Model> constraints, List<String> resultList) {
        IRI enumFirstPredicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
        IRI enumRestPredicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
        IRI enumEndObject = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
        for (Model m : constraints) {
            Model pEnumInfo = m.filter(firstNode, null, null);
            for (Statement st : pEnumInfo) {
                if (st.getPredicate().equals(enumFirstPredicate)) {
                    resultList.add(st.getObject().stringValue());
                }
                if (st.getObject().equals(enumEndObject)) {
                    return;
                }
                if (st.getPredicate().equals(enumRestPredicate)) {
                    enumerationToList((Resource) st.getObject(), constraints, resultList);
                }
            }
        }
    }

    private String extractNameFromIRI(String iri) {
        String name = iri;
        if (iri != null) name = iri.split("#")[1];
        return name;
    }

    private Property constructPropertyShape(String path, String node, String datatype, String minCount, String maxCount, String doc, List<String> aliases, List<String> enumerationList) {
        return Property.builder()
                .path(extractNameFromIRI(path))
                .node(extractNameFromIRI(node))
                .dataType(extractNameFromIRI(datatype))
                .minCount(minCount)
                .maxCount(maxCount)
                .doc(doc)
                .aliases(aliases)
                .in(enumerationList)
                .build();
    }

    public List<NodeShape> getNodeShapeList() {
        return this.nodeShapeList;
    }
}
