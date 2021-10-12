package TBD;

import java.util.List;

public class NodeShape {

    private final String nodeShapeID;
    private final String targetClass;
    private final List<String> enumeration;
    private final List<Property> propertyList;

    public NodeShape(String nodeShapeID, String targetClass, List<String> enumeration, List<Property> propertyList){
        this.nodeShapeID = nodeShapeID;
        this.targetClass = targetClass;
        this.enumeration = enumeration;
        this.propertyList = propertyList;
    }

}
