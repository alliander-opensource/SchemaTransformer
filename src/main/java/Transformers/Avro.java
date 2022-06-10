package Transformers;

import TBD.NodeShape;
import TBD.Property;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Avro {

    List<NodeShape> parents = new ArrayList<>();

    public Schema buildBaseRecord(List<NodeShape> nodeShapeList) {

        NodeShape rootObject = null;
        for (NodeShape ns : nodeShapeList) {
            if (ns.isRootObject()) rootObject = ns;
        }
        assert rootObject != null;
        parents.add(rootObject);

        String[] aliases = rootObject.getAliases().toArray(new String[0]);
        Schema base = SchemaBuilder
                .record(rootObject.getTargetClass())
                .doc(rootObject.getDoc())
                .aliases(aliases)
                .fields()
                .endRecord();

        base = buildRecordFields(base, rootObject, nodeShapeList);
        base = buildSuperClassFields(base, rootObject, nodeShapeList);

        return base;
    }

    public Schema buildRecordFields(Schema base, NodeShape rootObject, List<NodeShape> nodeShapeList) {
        for (Property property : rootObject.getPropertyList()) {
            List<Schema.Field> baseFields = base.getFields().stream()
                    .map(field -> new Schema.Field(field.name(), field.schema(), field.doc(), field.defaultVal()))
                    .collect(Collectors.toList());

            Schema schema = cardinalityToSchema(property.getMinCount(), property.getMaxCount(), property.getDataType(), property.getNode(), nodeShapeList);
            if (schema != null) baseFields.add(new Schema.Field(property.getPath(), schema, property.getDoc()));

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
        }
        return base;
    }

    private Schema buildSuperClassFields(Schema base, NodeShape targetObject, List<NodeShape> nodeShapeList){
        String subClassOf = targetObject.getSubClassOf();
        NodeShape superClass = null;
        if(subClassOf != null){
            for(NodeShape ns: nodeShapeList){
                if(subClassOf.equals(ns.getTargetClass())){
                    superClass = ns;
                }
            }
            if(superClass != null) {
                base = buildRecordFields(base, superClass, nodeShapeList);
                base = buildSuperClassFields(base, superClass, nodeShapeList);
            }
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
                        .doc(targetNode.getDoc())
                        .aliases(targetNode.getAliases().toArray(new String[0]))
                        .fields()
                        .endRecord();
                parents.add(targetNode);
                schema = buildRecordFields(base, targetNode, nodeShapeList);
                schema = buildCardinalityRecord(_minCount, _maxCount, schema);
                schema = buildSuperClassFields(schema, targetNode, nodeShapeList);
                if (parents.get(parents.size() - 1).equals(targetNode)) {
                    parents.remove(targetNode);
                }
            }
        }
        return schema;
    }

    private Schema buildCardinalityRecord(Integer minCount, Integer maxCount, Schema schema) {
        // 1 - 1
        schema = SchemaBuilder.builder().type(schema);
        if (maxCount.equals(2)) {
            if (minCount.equals(0)) {
                // 0 - *
                schema = SchemaBuilder.builder().unionOf().nullType().and().array().items().type(schema).endUnion();
            } else {
                // 1 - *
                schema = SchemaBuilder.builder().array().items(schema);
            }
        } else {
            if (minCount.equals(0)) {
                // 0 - 1
                schema = SchemaBuilder.builder().unionOf().nullType().and().type(schema).endUnion();
            }
        }
        return schema;
    }

    private Schema buildEnum(Integer minCount, Integer maxCount, NodeShape targetNode) {
        String[] enumeration = targetNode.getEnumeration().toArray(new String[0]);
        String[] aliases = targetNode.getAliases().toArray(new String[0]);
        // 1 - 1
        Schema schema = SchemaBuilder.builder().enumeration(targetNode.getTargetClass()).doc(targetNode.getDoc()).aliases(aliases).symbols(enumeration);
        if (maxCount.equals(2)) {
            if (minCount.equals(0)) {
                // 0 - *
                schema = SchemaBuilder.builder().unionOf().nullType().and().array().items().enumeration(targetNode.getTargetClass()).doc(targetNode.getDoc()).aliases(aliases).symbols(enumeration).endUnion();
            } else {
                // 1 - *
                schema = SchemaBuilder.builder().array().items().enumeration(targetNode.getTargetClass()).doc(targetNode.getDoc()).aliases(aliases).symbols(enumeration);
            }
        } else {
            if (minCount.equals(0)) {
                // 0 - 1
                schema = SchemaBuilder.builder().unionOf().nullType().and().enumeration(targetNode.getTargetClass()).doc(targetNode.getDoc()).aliases(aliases).symbols(enumeration).endUnion();
            }
        }
        return schema;
    }

    private Schema buildSchema(Integer minCount, Integer maxCount, String dataType) {
        //default 1 - 1 cardinality
        if (dataType.equals("dateTime")) dataType = "string";
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
