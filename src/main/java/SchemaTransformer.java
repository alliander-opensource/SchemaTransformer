import Consumer.FileReader;
import Consumer.RDFMap;
import TBD.NodeShapeConstructor;
import Transformers.Avro;
import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaTransformer implements Callable<Integer> {
    @Option(names = {"-c", "--config"}, paramLabel = "CONFIG FILE", description = "run config file for multiple profiles")
    File runConfig;

    @Option(names = {"-b", "--base-path"}, description = "absolute base path, supply if you are using -c")
    String basePath;

    @Option(names = {"-p", "--dx-prof"}, description = "path to profile directory, use with single profile")
    String inputFilePath;

    @Option(names = {"-o", "--output"}, description = "path for file output")
    String outPath;

    Set<String> paths = new LinkedHashSet<>();

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        if (runConfig != null && basePath != null && outPath != null){
            Pattern p = Pattern.compile(basePath);

            Stream<String> fileContents = Files.lines(runConfig.toPath());
            for(String path: fileContents.collect(Collectors.toList())){
                Matcher m = p.matcher(path);
                if (m.find()) {
                    path = path.substring(m.end());
                    if(StringUtils.countMatches(path, "/") > 1) {
                        paths.add(basePath + "/" + StringUtils.substringBetween(path, "/"));
                    }
                }
            }
        if (inputFilePath != null) paths.add(inputFilePath);

        for (String path : paths){
            executeLogic(path);
        }

        }
        return 0;
    }

    private void executeLogic(String path) throws IOException {
        FileReader fileReader = new FileReader(path);
        RDFMap rdfMap = new RDFMap(fileReader.getRDFFiles());
        List<Model> constraints = rdfMap.getConstraints();
        List<Model> vocabularies = rdfMap.getVocabularies();
        NodeShapeConstructor nodeShapeConstructor = new NodeShapeConstructor(constraints, vocabularies);
        Avro avro = new Avro();

        Schema result = avro.buildBaseRecord(nodeShapeConstructor.getNodeShapeList());
        System.out.println(result);
        avro.writeToAvro(result, Paths.get(path), outPath);
    }
}
