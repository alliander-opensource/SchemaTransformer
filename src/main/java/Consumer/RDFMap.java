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

    private Optional<Model> profile;
    private List<Model> constraints;
    private List<Model> vocabularies;

    private Optional<String> constraintSubject;
    private Optional<String> vocabularySubject;

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
        this.profile = rdfFiles.stream()
                .filter(RDFType::isProfile)
                .findFirst();

        this.profile.ifPresent(profile -> this.constraintSubject = this.rdfType.getProfileResources(profile, this.roleConstraints));

        if (constraintSubject.isPresent())
            this.constraints = this.rdfFiles
                    .stream()
                    .filter(m -> RDFType.isResource(m, this.constraintSubject.get()))
                    .collect(Collectors.toList());

        this.profile.ifPresent(profile -> this.vocabularySubject = this.rdfType.getProfileResources(profile, this.roleVocabulary));

        if (vocabularySubject.isPresent())
            this.vocabularies = this.rdfFiles
                    .stream()
                    .filter(m -> RDFType.isResource(m, this.vocabularySubject.get()))
                    .collect(Collectors.toList());
    }

    public Optional<Model> getProfile(){
        return this.profile;
    }

    public List<Model> getConstraints(){
        return this.constraints;
    }

    public List<Model> getVocabularies(){return this.vocabularies;}
}
