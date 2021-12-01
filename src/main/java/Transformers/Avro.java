package Transformers;

import TBD.NodeShape;
import TBD.Property;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Avro {

    List<NodeShape> parents = new ArrayList<>();

    public <T> GenericRecord pojoToRecord(T model) throws IOException {
        Schema schema = ReflectData.get().getSchema(model.getClass());

        ReflectDatumWriter<T> datumWriter = new ReflectDatumWriter<>(schema);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        datumWriter.write(model, encoder);
        encoder.flush();

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(outputStream.toByteArray(), null);

        return datumReader.read(null, decoder);
    }

    public void buildSchema(NodeShape nodeShape) {
        Schema schema = SchemaBuilder
                .record("B")
                .doc("This is a sub-class")
                .aliases("Bb")
                .fields()
                .name("id").type().stringType().noDefault()
                .name("abc").type().unionOf().nullType().and().array().items().type("string").endUnion().noDefault()
                .name("bcd").type().array().items().type("double").noDefault()
                .name("FromAtoC").type().enumeration("C").aliases("Cc").doc("This is a class with named elements").symbols("individual1", "individual2", "individual3").noDefault()
                .name("FromBToD")
                .doc("Association from B to D")
                .type().unionOf().nullType().and().record("D").aliases("Dd").doc("This is yet another class").fields()
                .name("id").type().stringType().noDefault()
                .name("def").type().unionOf().nullType().and().intType().endUnion().noDefault().endRecord().endUnion().noDefault()
                .name("FromBToDButSomehowDifferent")
                .doc("No clue")
                .type("D").noDefault()
                .endRecord();
        System.out.println("hello");
    }

    public Schema buildBaseRecord(List<NodeShape> nodeShapeList) {

        NodeShape rootObject = null;
        for (NodeShape ns : nodeShapeList) {
            if (ns.isRootObject()) rootObject = ns;
        }
        assert rootObject != null;
        parents.add(rootObject);

        Schema base = SchemaBuilder
                .record(rootObject.getTargetClass())
                .doc("to be implemented")
                .aliases("Bb")
                .fields()
                .endRecord();

        base = buildRecordFields(base, rootObject, nodeShapeList);

        System.out.println("the end");
        return base;
    }

    public Schema buildRecordFields(Schema base, NodeShape rootObject, List<NodeShape> nodeShapeList) {
        return buildRecordFields(base, rootObject, nodeShapeList, null, null);
    }

    public Schema buildRecordFields(Schema base, NodeShape rootObject, List<NodeShape> nodeShapeList, String minCount, String maxCount) {
        for (Property property : rootObject.getPropertyList()) {
            List<Schema.Field> baseFields = base.getFields().stream()
                    .map(field -> new Schema.Field(field.name(), field.schema(), field.doc(), field.defaultVal()))
                    .collect(Collectors.toList());

            String _minCount = minCount != null ? minCount : property.getMinCount();
            String _maxCount = maxCount != null ? maxCount : property.getMaxCount();

            Schema schema = cardinalityToSchema(_minCount, _maxCount, property.getDataType(), property.getNode(), nodeShapeList);
            if (schema != null) baseFields.add(new Schema.Field(property.getPath(), schema));

//                baseFields.add(new Schema.Field("FromAToC", SchemaBuilder.builder().enumeration("C").aliases("Cc").doc("This is a class with named elements").symbols("individual1", "individual2", "individual3")));
//                baseFields.add(new Schema.Field("FromBToD", SchemaBuilder.builder().record("D").doc("This is yet another class").fields().name("id").type().stringType().noDefault().name("def").type().unionOf().nullType().and().intType().endUnion().noDefault().endRecord()));
            Schema newSchema = Schema.createRecord(
                    base.getName(),
                    base.getDoc(),
                    "",
                    false,
                    baseFields);
            for (String alias : base.getAliases()) {
                newSchema.addAlias(alias);
            }
            base = newSchema;
            System.out.println("hello");
        }
        return base;
    }

    private Schema cardinalityToSchema(String minCount, String maxCount, String dataType, String node, List<NodeShape> nodeShapeList) {
        // normalize minCount and maxCount because from the dx-prof its perfectly normal to receive a 2-5 cardinality,
        // avro does not understand that, so we have to normalize it
        // if the min cardinality is greater or equal than 1 we normalize it to 1, 0 stays 0 obviously
        Integer _minCount = Integer.parseInt(minCount) >= 1 ? 1 : 0;
        // if the max cardinality is greater than 1 we normalize it to 2 because * is a string, 1 stays 1 obviously
        // default cardinality of *
        Integer _maxCount = 2;
        if (!maxCount.equals("*")) _maxCount = Integer.parseInt(maxCount) > 1 ? 2 : 1;

        Schema schema = null;
        if (dataType != null) {
            schema = buildSchema(_minCount, _maxCount, dataType);
        }

        if (node != null) {
            NodeShape targetNode = null;
            for (NodeShape nodeShape : nodeShapeList) {
                if (nodeShape.getNodeShapeID().equals(node)) targetNode = nodeShape;
            }
            assert targetNode != null;
            if (!targetNode.getEnumeration().isEmpty()) {
                schema = buildEnum(_minCount, _maxCount, targetNode);
            } else if (!targetNode.getPropertyList().isEmpty() && !parents.contains(targetNode)) {
                Schema base = SchemaBuilder
                        .record(targetNode.getTargetClass())
                        .doc("to be implemented")
                        .aliases("Bb")
                        .fields()
                        .endRecord();
                parents.add(targetNode);
                schema = buildRecordFields(base, targetNode, nodeShapeList, _minCount.toString(), _maxCount.toString());
                if (parents.get(parents.size() -1).equals(targetNode)) {
                    parents.remove(targetNode);
                }
            }
        }
        return schema;
    }

    private Schema buildEnum(Integer minCount, Integer maxCount, NodeShape targetNode) {
        String[] enumeration = targetNode.getEnumeration().toArray(new String[0]);
        // 1 - 1
        Schema schema = SchemaBuilder.builder().enumeration(targetNode.getTargetClass()).symbols(enumeration);
        if (maxCount.equals(2)) {
            if (minCount.equals(0)) {
                // 0 - *
                schema = SchemaBuilder.builder().unionOf().nullType().and().array().items().enumeration(targetNode.getTargetClass()).symbols(enumeration).endUnion();
            } else {
                // 1 - *
                schema = SchemaBuilder.builder().array().items().enumeration(targetNode.getTargetClass()).symbols(enumeration);
            }
        } else {
            if (minCount.equals(0)){
                // 0 - 1
                schema = SchemaBuilder.builder().unionOf().nullType().and().enumeration(targetNode.getTargetClass()).symbols(enumeration).endUnion();
            }
        }
        return schema;
    }

    private Schema buildSchema(Integer minCount, Integer maxCount, String dataType) {
        //default 1 - 1 cardinality
        Schema schema = SchemaBuilder.builder().type(dataType);

        if (maxCount.equals(2)) {
            if (minCount.equals(0)) {
                // 0 - *
                schema = SchemaBuilder.builder().unionOf().nullType().and().array().items().type(dataType).endUnion();
            } else {
                // 1 - *
                schema = SchemaBuilder.builder().array().items().type(dataType);
            }
        } else {
            if (minCount.equals(0)) {
                // 0 - 1
                schema = SchemaBuilder.builder().unionOf().nullType().and().type(dataType).endUnion();
            }
        }
        return schema;
    }
}
