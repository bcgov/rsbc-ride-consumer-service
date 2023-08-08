package bcgov.rsbc.ride.kafka.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeolocationRequest {
    private String businessId;
    private String violationHighwayDesc;
    private String violationCityName;
}
