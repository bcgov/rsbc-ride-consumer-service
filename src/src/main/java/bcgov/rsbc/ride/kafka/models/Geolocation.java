package bcgov.rsbc.ride.kafka.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import java.math.BigDecimal;

@Data
@Builder
public class Geolocation {
    @JsonProperty("business_program")
    private String businessProgram;
    @JsonProperty("business_type")
    private String businessType;
    @JsonProperty("business_id")
    private String businessId;
    @JsonProperty("long")
    private BigDecimal longitude;
    @JsonProperty("lat")
    private BigDecimal latitude;
    @JsonProperty("precision")
    private String precision;
    @JsonProperty("requested_address")
    private String requestedAddress;
    @JsonProperty("submitted_address")
    private String submittedAddress;
    @JsonProperty("databc_long")
    private BigDecimal databcLong;
    @JsonProperty("databc_lat")
    private BigDecimal databcLat;
    @JsonProperty("databc_score")
    private Integer databcScore;
    @JsonProperty("databc_precision")
    private String databcPrecision;
    @JsonProperty("full_address")
    private String fullAddress;
    @JsonProperty("faults")
    private JSONArray faults;
}
