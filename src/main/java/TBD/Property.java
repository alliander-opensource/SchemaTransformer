package TBD;

import lombok.Builder;
import lombok.Getter;
import org.apache.avro.reflect.Nullable;

import java.util.List;

@Builder
@Getter
public class Property {
    private String path;
    private String dataType; //type
    private String node; //record || enum
    private String minCount;
    private String maxCount;
    private List<String> in;
}
