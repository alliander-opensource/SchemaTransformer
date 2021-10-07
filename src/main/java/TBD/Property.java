package TBD;

import java.util.List;
import java.util.Optional;

public class Property {
    public static class Builder {
        private final String path;
        private String dataType;
        private String node;
        private String minCount;
        private String maxCount;
        private List<String> in;

        public Builder(String path){
            this.path = path;
        }

        public Builder dataType(String dataType){
            this.dataType = dataType;
            return this;
        }

        public Builder node(String node){
            this.node = node;
            return this;
        }

        public Builder minCount(String minCount){
            this.minCount = minCount;
            return this;
        }

        public Builder maxCount(String maxCount){
            this.maxCount = maxCount;
            return this;
        }

        public Builder in(List<String> in){
            this.in = in;
            return this;
        }

        public Property build(){
            Property property = new Property();
            property.path = this.path;
            if(Optional.ofNullable(this.dataType).isPresent() && Optional.ofNullable(this.node).isPresent()){
                throw new IllegalStateException("both 'datatype' and 'node' can not co-exist, specify one or the other.");
            }
            property.dataType = this.dataType;
            property.node = this.node;
            property.minCount = this.minCount;
            property.maxCount = this.maxCount;
            property.in = this.in;

            return property;
        }
    }

    private String path;
    private String dataType;
    private String node;
    private String minCount;
    private String maxCount;
    private List<String> in;

    private Property(){}
}
