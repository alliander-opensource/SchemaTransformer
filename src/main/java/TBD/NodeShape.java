package TBD;

import java.util.List;

public class NodeShape {

    private final String nodeShapeID;
    private final String targetClass;
    private final boolean rootObject;
    private final List<String> enumeration;
    private final List<Property> propertyList;

    public NodeShape(String nodeShapeID, String targetClass, boolean rootObject, List<String> enumeration, List<Property> propertyList){
        this.nodeShapeID = nodeShapeID;
        this.targetClass = targetClass;
        this.rootObject = rootObject;
        this.enumeration = enumeration;
        this.propertyList = propertyList;
    }

}
