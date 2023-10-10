package bcgov.rsbc.ride.kafka.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class ApproximateGeolocationAdapter {

    @JsonProperty("business_program")
    private String business_program;

    @JsonProperty("business_type")
    private String business_type;

    @JsonProperty("business_id")
    private String business_id;

    @JsonProperty("long")
    private String long$;

    @JsonProperty("lat")
    private String lat;

    @JsonProperty("precision")
    private String precision;

    @JsonProperty("requested_address")
    private String requested_address;

    @JsonProperty("submitted_address")
    private String submitted_address;

    @JsonProperty("full_address")
    private String full_address;

    @JsonProperty("databc_long")
    private String databc_long;

    @JsonProperty("databc_lat")
    private String databc_lat;

    @JsonProperty("databc_score")
    private String databc_score;

    @JsonProperty("alternate_long")
    private String alternate_long;

    @JsonProperty("alternate_lat")
    private String alternate_lat;

    @JsonProperty("alternate_score")
    private String alternate_score;

    @JsonProperty("alternate_precision")
    private String alternate_precision;

    @JsonProperty("faults")
    private String faults;


    public ObjectNode toJson()  {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(this);
            return (ObjectNode) objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}