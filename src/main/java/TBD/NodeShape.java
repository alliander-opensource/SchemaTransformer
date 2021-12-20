package TBD;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NodeShape {
    private String nodeShapeID;
    private String targetClass;
    private String doc;
    private String subClassOf;
    private boolean rootObject;
    private List<String> aliases;
    private List<String> enumeration;
    private List<Property> propertyList;
}
