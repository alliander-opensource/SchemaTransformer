package TBD;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Values;

import java.util.*;

public class NodeShapeConstructor {

    private final List<Model> constraints;
    private final Set<IRI> nodes = new HashSet<>();
    private final Set<Resource> propertyNodes = new HashSet<>();
//    private final List<Property> propertyList = new Map<>();

    public NodeShapeConstructor(List<Model> constraints){
        this.constraints = constraints;
        getUniqueNodeShapes();
        getUniquePropertyNodes();
        getPropertyNodeAttributes();
    }

    public void getUniqueNodeShapes(){
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

    public void getUniquePropertyNodes(){
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

    public void getPropertyNodeAttributes(){
        IRI pathPredicate = Values.iri("http://www.w3.org/ns/shacl#path");
        IRI nodePredicate = Values.iri("http://www.w3.org/ns/shacl#node");
        IRI minCountPredicate = Values.iri("http://www.w3.org/ns/shacl#minCount");
        IRI maxCountPredicate = Values.iri("http://www.w3.org/ns/shacl#maxCount");
        IRI datatypePredicate = Values.iri("http://www.w3.org/ns/shacl#datatype");
        IRI enumerationPredicate = Values.iri("http://www.w3.org/ns/shacl#in");
        for (Resource pNode: this.propertyNodes){

            String path;
            String node;
            String datatype;
            Optional<String> minCount = Optional.of("0");
            Optional<String> maxCount = Optional.of("*");
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
                        minCount = Optional.of(st.getObject().stringValue());
                    }
                    if(st.getPredicate().equals(maxCountPredicate)){
                        maxCount = Optional.of(st.getObject().stringValue());
                    }
                    if(st.getPredicate().equals(enumerationPredicate)){
                        propertyListConstructor((Resource) st.getObject(), enumerationList);
                    }

                }
            }
//            constructPropertyShape(pNode, path, node, datatype, minCount.get(), maxCount.get(), enumerationList);
        }
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

    private void constructPropertyShape(Resource propertyNode, String path, String node, String datatype, String minCount, String maxCount, List<String> enumerationList){
        IRI predicate = Values.iri("http://www.w3.org/ns/shacl#property");
        for(Model constraint: this.constraints){
            for(Statement st: constraint){
                if (st.getPredicate().equals(predicate) && st.getObject().equals(propertyNode)){
                    Property property = new Property.Builder(path)
                            .node(node)
                            .dataType(datatype)
                            .minCount(minCount)
                            .maxCount(maxCount)
                            .in(enumerationList)
                            .build();

//                    this.propertyList.add(property);
                }
            }
        }
    }
}
