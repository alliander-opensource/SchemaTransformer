package TBD;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NodeShape {
    private String nodeShapeID;
    private String targetClass;
    private boolean rootObject;
    private String doc;
    private List<String> aliases;
    private List<String> enumeration;
    private List<Property> propertyList;
}
