package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.GeolocationRequest;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
import bcgov.rsbc.ride.kafka.service.GeocoderService;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import bcgov.rsbc.ride.kafka.service.ReconService;

@Slf4j
@ApplicationScoped
public class GeolocationEvent extends EtkEventHandler<IssuanceRecord, GeolocationRequest>{

    private static final Logger logger = Logger.getLogger(GeolocationEvent.class);

    @Inject
    GeocoderService geocoderService;

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.geolocation")
    Optional<List<String>> primaryKey;

    @Override
    protected GeolocationRequest mapEvent(IssuanceRecord issuanceRecord) {
        return GeolocationRequest.builder()
                .businessId(issuanceRecord.getTicketNumber())
                .violationHighwayDesc(issuanceRecord.getViolationHighwayDesc())
                .violationCityName(issuanceRecord.getViolationCityName())
                .build();
    }

    @Override
    public void execute(GeolocationRequest event,String key) {
        logger.info("GeolocationRequest Event received: " + event);
        reconService.updateMainStagingStatus(key,"consumer_process");
        geocoderService.callGeocoderApi(event).thenAccept(geoloc ->
                rideAdapterService.sendData(List.of(geoloc),"gis", "geolocations", primaryKey.orElse(null),key));
    }
}
