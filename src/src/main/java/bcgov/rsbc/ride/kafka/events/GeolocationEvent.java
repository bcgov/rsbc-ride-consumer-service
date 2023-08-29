package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.GeolocationRequest;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
import bcgov.rsbc.ride.kafka.service.GeocoderService;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class GeolocationEvent implements EtkEventHandler<IssuanceRecord, GeolocationRequest>{

    private static final Logger logger = Logger.getLogger(GeolocationEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    GeocoderService geocoderService;

    @Override
    public List<Class<GeolocationRequest>> getEventTypeToHandler() { return List.of(GeolocationRequest.class); }

    @Override
    public GeolocationRequest mapperEvent(IssuanceRecord issuanceRecord) {
        return GeolocationRequest.builder()
                .businessId(issuanceRecord.getTicketNumber())
                .violationHighwayDesc(issuanceRecord.getViolationHighwayDesc())
                .violationCityName(issuanceRecord.getViolationCityName())
                .build();
    }

    @Override
    public void execute(GeolocationRequest event) {
        logger.info("GeolocationRequest Event received: " + event);
        geocoderService.callGeocoderApi(event).thenAccept(geoloc ->
                rideAdapterService.sendData(geoloc, "gis.geolocations"));
    }
}
