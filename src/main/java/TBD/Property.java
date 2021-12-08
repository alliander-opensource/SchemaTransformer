package TBD;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class Property {
    private String path;
    private String dataType;
    private String node;
    private String minCount;
    private String maxCount;
    private String doc;
    private List<String> aliases;
    private List<String> in;
}
