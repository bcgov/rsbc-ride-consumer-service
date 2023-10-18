package bcgov.rsbc.ride.kafka.models;

import lombok.Data;

@Data
public class PreciseGeolocationAdapter {
    private String ticket_number;
    private String server_code;
    private String x_value;
    private String y_value;
    private Object event;
}
