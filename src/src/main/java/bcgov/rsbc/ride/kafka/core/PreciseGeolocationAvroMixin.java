package bcgov.rsbc.ride.kafka.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PreciseGeolocationAvroMixin {
    @JsonCreator
    public PreciseGeolocationAvroMixin(@JsonProperty("ticket_number") String ticket_number,
                                       @JsonProperty("server_code") String server_code,
                                       @JsonProperty("x_value") String x_value,
                                       @JsonProperty("y_value") String y_value) {
    }
}
