import Consumer.FileReader;
import Consumer.RDFMap;
import TBD.NodeShapeConstructor;
import Transformers.Avro;
import org.apache.avro.Schema;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.model.Model;

import java.io.IOException;
import java.util.List;

public class Main{
    public static void main(String[] args) throws IOException {

        Options options = new Options();

        Option input = new Option("p", "dx-prof", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null; //not a good practice, it serves it purpose

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("dx-prof");

        System.out.println(inputFilePath);

        FileReader fileReader = new FileReader(inputFilePath);
        RDFMap rdfMap = new RDFMap(fileReader.getRDFFiles());
        List<Model> constraints = rdfMap.getConstraints();
        List<Model> vocabularies = rdfMap.getVocabularies();
        NodeShapeConstructor nodeShapeConstructor = new NodeShapeConstructor(constraints, vocabularies);
        Avro avro = new Avro();

        Schema result = avro.buildBaseRecord(nodeShapeConstructor.getNodeShapeList());
        System.out.println(result);
        avro.writeToAvro(avro.buildBaseRecord(nodeShapeConstructor.getNodeShapeList()));


    }
}