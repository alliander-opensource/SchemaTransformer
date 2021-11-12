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
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Avro {
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
                .endRecord();
        System.out.println("hello");
    }

    public Schema buildRecord(List<NodeShape> nodeShapes){

            NodeShape rootObject = null;

            for(NodeShape ns: nodeShapes){
                if (ns.isRootObject()) rootObject = ns;
            }

        assert rootObject != null;
        Schema base = SchemaBuilder
                    .record(rootObject.getTargetClass())
                    .doc("to be implemented")
                    .aliases("Bb")
                    .fields()
                    .endRecord();

            for(Property property: nodeShape.getPropertyList()) {
                List<Schema.Field> baseFields = base.getFields().stream()
                        .map(field -> new Schema.Field(field.name(), field.schema(), field.doc(), field.defaultVal()))
                        .collect(Collectors.toList());

                baseFields.add(new Schema.Field(property.getPath(), cardinalityToSchema(property.getMinCount(), property.getMaxCount(), property.getDataType(), property.getNode())));

//                baseFields.add(new Schema.Field("FromAToC", SchemaBuilder.builder().enumeration("C").aliases("Cc").doc("This is a class with named elements").symbols("individual1", "individual2", "individual3")));
//                baseFields.add(new Schema.Field("FromBToD", SchemaBuilder.builder().record("D").doc("This is yet another class").fields().name("id").type().stringType().noDefault().name("def").type().unionOf().nullType().and().intType().endUnion().noDefault().endRecord()));
                Schema newSchema = Schema.createRecord(
                        base.getName(),
                        base.getDoc(),
                        "",
                        false,
                        baseFields);
                for(String alias : base.getAliases()){
                    newSchema.addAlias(alias);
                }
                System.out.println("hello");
            }
            return base;
    }

    private Schema cardinalityToSchema(String minCount, String maxCount, String dataType, String node){
        String type = dataType != null ? dataType : node;

        //default 1 - 1 cardinality
        Schema schema = SchemaBuilder.builder().type(type);

        Integer _minCount = Integer.parseInt(minCount);
        if (!maxCount.equals("*")){
            int _maxCount = Integer.parseInt(maxCount);
            if (_minCount < _maxCount){
                // 0 - 1
                schema = SchemaBuilder.builder().unionOf().nullType().and().type(type).endUnion();
            }
        } else {
            if(_minCount.equals(0)){
                // 0 - *
                schema = SchemaBuilder.builder().unionOf().nullType().and().array().items().type(type).endUnion();
            } else {
                // 1 - *
                schema = SchemaBuilder.builder().array().items().type(type);
            }
        }
        return schema;
    }
}
