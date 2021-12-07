package Consumer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RDFMap {

    private final Model profile;
    private final List<Model> constraints;
    private final List<Model> vocabularies;

    private final RDFType rdfType;

    public RDFMap(List<Model> rdfFiles) {
        this.profile = extractProfile(rdfFiles);
        this.rdfType = new RDFType();

        IRI roleConstraints = Values.iri("http://www.w3.org/ns/dx/prof/role/constraints");
        this.constraints = extractLogicalModels(rdfFiles, roleConstraints);

        IRI roleVocabulary = Values.iri("http://www.w3.org/ns/dx/prof/role/vocabulary");
        this.vocabularies = extractLogicalModels(rdfFiles, roleVocabulary);
    }

    private Model extractProfile(List<Model> rdfFiles) {
        Optional<Model> profile = rdfFiles.stream()
                .filter(RDFType::isProfile)
                .findFirst();

        return profile.orElseThrow(() -> new IllegalStateException("Profile not found, did you supply the correct files?"));
    }

    private List<Model> extractLogicalModels(List<Model> rdfFiles, IRI logicalModel) {
        Optional<String> subject = this.rdfType.getProfileResources(this.profile, logicalModel);

        return rdfFiles.stream()
                .filter(model -> RDFType.isResource(model, subject.orElseThrow(
                        () -> new IllegalStateException("No logical model found for: " + logicalModel.toString())
                )))
                .collect(Collectors.toList());
    }

    public Model getProfile() {
        return this.profile;
    }

    public List<Model> getConstraints() {
        return this.constraints;
    }

    public List<Model> getVocabularies() {
        return this.vocabularies;
    }
}
