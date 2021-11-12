package TBD;

import lombok.Builder;
import lombok.Getter;
import org.apache.avro.reflect.Nullable;

import java.util.List;

@Builder
@Getter
public class NodeShape {
    private String nodeShapeID;
    private String targetClass;
    private boolean rootObject;
    private List<String> enumeration;
    private List<Property> propertyList;
}
