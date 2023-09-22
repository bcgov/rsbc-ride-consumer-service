package bcgov.rsbc.ride.kafka.models;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class GeolocationRequest {
    private String businessId;
    private String violationHighwayDesc;
    private String violationCityName;
}
