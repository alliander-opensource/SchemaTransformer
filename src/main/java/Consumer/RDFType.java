package Consumer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;

import java.util.Optional;

public class RDFType {

    public RDFType(){}

    public static boolean isProfile(Model rdfModel){
        IRI profilePredicate = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI profileObject = Values.iri("http://www.w3.org/ns/dx/prof/Profile");

        for(Statement st: rdfModel){
            if (st.getPredicate().equals(profilePredicate) && st.getObject().equals(profileObject)){
                return true;
            }
        }
        return false;
    }

    public Optional<String> getProfileResources(Model rdfModel, IRI resourceIRI) {
        IRI hasRolePredicate = Values.iri("http://www.w3.org/ns/dx/prof/hasRole");
        IRI hasArtifactPredicate = Values.iri("http://www.w3.org/ns/dx/prof/hasArtifact");

        Optional<IRI> _resourceIRI = Optional.empty();
        Optional<String> resource = Optional.empty();

        for (Statement st : rdfModel) {
            if (st.getPredicate().equals(hasRolePredicate) && st.getObject().equals(resourceIRI)) {
                _resourceIRI = Optional.of((IRI) st.getSubject());
            }
        }
        for (Statement st : rdfModel) {
            if (_resourceIRI.isPresent() && st.getSubject().equals(_resourceIRI.get()) && st.getPredicate().equals(hasArtifactPredicate)) {
                resource = Optional.of(st.getObject().toString());
            }
        }
        return resource;
    }

    public static boolean isResource(Model rdfModel, String resourceSubject){
        for(Statement st: rdfModel){
            if (st.getSubject().stringValue().contains(resourceSubject)){
                return true;
            }
        }
        return false;
    }
}
