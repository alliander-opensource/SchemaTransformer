package Consumer;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {
    private List<Path> paths;
    private List<Model> rdfFiles;

    public FileReader(String directoryPath) throws IOException {
        listFilesForFolder(directoryPath);
        listRDFFiles();
    }

    private void listFilesForFolder(String directoryPath) throws IOException {
        Stream<Path> paths = Files.walk(Paths.get(directoryPath));
        this.paths = paths.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
    }

    private void listRDFFiles() throws IOException {
        this.rdfFiles = new ArrayList<>();

        for(Path filePath: paths){
            InputStream input = new FileInputStream(String.valueOf(filePath));
            Optional<Model> model = getRDFFormatModel(input);
            model.ifPresent(m -> this.rdfFiles.add(m));
        }
    }

    private Optional<Model> getRDFFormatModel(InputStream input){
        List<RDFFormat> formatList = List.of(RDFFormat.TURTLE, RDFFormat.RDFXML);
        Optional<Model> model = Optional.empty();
        for (RDFFormat format: formatList) {
            try {
                model = Optional.of(Rio.parse(input, "", format));
            } catch (RDFParseException | IOException e) {
                // Not an RDF file, which is fine because we don't want those
            }
        }
        return model;
    }

    public List<Model> getRDFFiles(){
        return this.rdfFiles;
    }
}
