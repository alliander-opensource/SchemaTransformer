package TBD;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Values;

import java.util.*;

public class NodeShapeConstructor {

    private final List<Model> constraints;
    private final Set<IRI> nodes = new HashSet<>();
    private final Set<Resource> propertyNodes = new HashSet<>();
    private List<NodeShape> nodeShapeList;

    public NodeShapeConstructor(List<Model> constraints){
        this.constraints = constraints;
        getUniqueNodeShapes();
        getUniquePropertyNodes();
        constructNodeShapes();
    }

    private void getUniqueNodeShapes(){
        IRI predicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI object = Values.iri("http://www.w3.org/ns/shacl#NodeShape");
        for(Model m : this.constraints){
            for(Statement st: m){
                if(st.getPredicate().equals(predicate) && st.getObject().equals(object)){
                   this.nodes.add((IRI) st.getSubject());
                }
            }
        }
    }

    private void getUniquePropertyNodes(){
        IRI predicate = Values.iri("http://www.w3.org/ns/shacl#property");
        for(IRI node: this.nodes){
            for(Model m : this.constraints){
                for(Statement st : m){
                    if(st.getSubject().equals(node) && st.getPredicate().equals(predicate)){
                        this.propertyNodes.add((Resource) st.getObject());
                    }
                }
            }
        }
    }

    private void constructNodeShapes(){
        IRI targetClassPredicate = Values.iri("http://www.w3.org/ns/shacl#TargetClass");
        List<NodeShape> nodeShapeList = new ArrayList<>();
        for(IRI node: this.nodes){
            List<Property> propertyList = new ArrayList<>();
            String targetClass = null;
            for(Resource pNode : this.propertyNodes){
                for(Model m : this.constraints){
                    for(Statement st : m){
                        if(st.getPredicate().equals(targetClassPredicate) && st.getSubject().equals(node)){
                            targetClass = st.getObject().stringValue();
                        }
                        if(st.getSubject().equals(node) && st.getObject().equals(pNode)){
                            propertyList.add(getPropertyNodeAttributes(pNode));
                        }
                    }
                }
            }
            nodeShapeList.add(new NodeShape(node.stringValue(), targetClass, propertyList));
        }
        this.nodeShapeList = nodeShapeList;
    }

    private Property getPropertyNodeAttributes(Resource pNode){
        IRI pathPredicate = Values.iri("http://www.w3.org/ns/shacl#path");
        IRI nodePredicate = Values.iri("http://www.w3.org/ns/shacl#node");
        IRI minCountPredicate = Values.iri("http://www.w3.org/ns/shacl#minCount");
        IRI maxCountPredicate = Values.iri("http://www.w3.org/ns/shacl#maxCount");
        IRI datatypePredicate = Values.iri("http://www.w3.org/ns/shacl#datatype");
        IRI enumerationPredicate = Values.iri("http://www.w3.org/ns/shacl#in");

            String path = null;
            String node = null;
            String datatype = null;
            String minCount = "0";
            String maxCount = "*";
            List<String> enumerationList = new ArrayList<>();

            for (Model constraint : this.constraints) {
                Model pNodeInfo = constraint.filter(pNode, null, null);
                for (Statement st : pNodeInfo){
                    if(st.getPredicate().equals(pathPredicate)){
                        path = st.getObject().stringValue();
                    }
                    if(st.getPredicate().equals(nodePredicate)){
                        node = st.getObject().stringValue();
                    }
                    if(st.getPredicate().equals(datatypePredicate)){
                        datatype = st.getObject().stringValue();
                    }
                    if(st.getPredicate().equals(minCountPredicate)){
                        minCount = st.getObject().stringValue();
                    }
                    if(st.getPredicate().equals(maxCountPredicate)){
                        maxCount = st.getObject().stringValue();
                    }
                    if(st.getPredicate().equals(enumerationPredicate)){
                        propertyListConstructor((Resource) st.getObject(), enumerationList);
                    }
                }
            }
            return constructPropertyShape(path, node, datatype, minCount, maxCount, enumerationList);
    }

    private void propertyListConstructor(Resource firstNode, List<String> resultList){
        IRI enumFirstPredicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
        IRI enumRestPredicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
        IRI enumEndObject = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
        for(Model m : this.constraints){
            Model pEnumInfo = m.filter(firstNode, null, null);
            for(Statement st : pEnumInfo){
                if(st.getPredicate().equals(enumFirstPredicate)){
                    resultList.add(st.getObject().stringValue());
                }
                if(st.getObject().equals(enumEndObject)){
                    return;
                }
                if(st.getPredicate().equals(enumRestPredicate)){
                    propertyListConstructor((Resource) st.getObject(), resultList);
                }
            }
        }
    }

    private Property constructPropertyShape(String path, String node, String datatype, String minCount, String maxCount, List<String> enumerationList){
        return new Property.Builder(path)
                .node(node)
                .dataType(datatype)
                .minCount(minCount)
                .maxCount(maxCount)
                .in(enumerationList)
                .build();
    }

    public List<NodeShape> getNodeShapeList() {
        return this.nodeShapeList;
    }
}
