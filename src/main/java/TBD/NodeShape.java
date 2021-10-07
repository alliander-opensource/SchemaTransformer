package TBD;

import java.util.List;

public class NodeShape {

    private final String targetClass;
    private final List<Property> propertyList;

    public NodeShape(String targetClass, List<Property> propertyList){
        this.targetClass = targetClass;
        this.propertyList = propertyList;
    }

}
