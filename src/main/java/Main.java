import Consumer.FileReader;
import Consumer.RDFMap;
import TBD.NodeShapeConstructor;
import Transformers.Avro;
import org.eclipse.rdf4j.model.Model;

import java.io.IOException;
import java.util.List;

public class Main{
    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader("src/main/resources");
        RDFMap rdfMap = new RDFMap(fileReader.getRDFFiles());
        List<Model> modelList = rdfMap.getConstraints();
        List<Model> voca = rdfMap.getVocabularies();
        NodeShapeConstructor nodeShapeConstructor = new NodeShapeConstructor(modelList);
        Avro avro = new Avro();

        avro.buildSchema(nodeShapeConstructor.getNodeShapeList().get(0));
        avro.buildBaseRecord(nodeShapeConstructor.getNodeShapeList());


    }
}