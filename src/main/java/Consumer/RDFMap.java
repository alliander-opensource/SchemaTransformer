package Consumer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RDFMap {
    private final List<Model> rdfFiles;

    private Model profile;
    private List<Model> constraints;
    private List<Model> vocabularies;

    private final RDFType rdfType;
    private final IRI roleConstraints = Values.iri("http://www.w3.org/ns/dx/prof/role/constraints");
    private final IRI roleVocabulary = Values.iri("http://www.w3.org/ns/dx/prof/role/vocabulary");

    public RDFMap(List<Model> rdfFiles) {
        this.constraints = new ArrayList<>();
        this.vocabularies = new ArrayList<>();
        this.rdfFiles = rdfFiles;
        this.rdfType = new RDFType();
        mapRDFModels();
    }

    private void mapRDFModels() {
        Optional<Model> profile = rdfFiles.stream()
                .filter(RDFType::isProfile)
                .findFirst();

        this.profile = profile.orElseThrow(() -> new IllegalStateException("Profile not found, did you supply the correct files?"));

        Optional<String> constraintSubject = this.rdfType.getProfileResources(this.profile, this.roleConstraints);

        constraintSubject.ifPresent(subject -> this.constraints = this.rdfFiles
                .stream()
                .filter(model -> RDFType.isResource(model, subject))
                .collect(Collectors.toList()));

        Optional<String> vocabularySubject = this.rdfType.getProfileResources(this.profile, this.roleVocabulary);

        vocabularySubject.ifPresent(subject -> this.vocabularies = this.rdfFiles
                .stream()
                .filter(model -> RDFType.isResource(model, subject))
                .collect(Collectors.toList()));
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
