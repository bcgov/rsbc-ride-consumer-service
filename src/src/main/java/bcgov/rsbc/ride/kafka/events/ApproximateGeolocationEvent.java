package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.EventRecord;
import bcgov.rsbc.ride.kafka.models.GeolocationRequest;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
import bcgov.rsbc.ride.kafka.service.BackoffExecution.BackoffConfig;
import bcgov.rsbc.ride.kafka.service.GeocoderService;
import bcgov.rsbc.ride.kafka.service.ReconService;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ApproximateGeolocationEvent extends EtkEventHandler<IssuanceRecord, GeolocationRequest>{

    private static final Logger logger = Logger.getLogger(ApproximateGeolocationEvent.class);

    @Inject
    GeocoderService geocoderService;

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.geolocation")
    Optional<List<String>> primaryKey;

    @ConfigProperty(name = "ride.adapter.primarykey.events")
    Optional<List<String>> evtprimaryKey;

    @Override
    protected GeolocationRequest mapEvent(IssuanceRecord issuanceRecord) {
        return GeolocationRequest.builder()
                .businessId(issuanceRecord.getTicketNumber())
                .violationHighwayDesc(issuanceRecord.getViolationHighwayDesc())
                .violationCityName(issuanceRecord.getViolationCityName())
                .event(issuanceRecord.getEvent())
                .build();
    }

    @Override
    public void execute(GeolocationRequest event, String key) {
        logger.info("GeolocationRequest Event received: " + event);

        String eventId = event.event().getId();
        EventRecord eventRecord = event.event();
        setEventId(event, eventId);
        JsonObject eventPayload = JsonObject.mapFrom(event);

        eventPayload.remove("event");
        String rideEvtID=key;

        logger.info("GeolocationRequest Event received: " + eventPayload);
        BackoffConfig backoffConfig = BackoffConfig.builder()
                .maxRetries(0)
                .timeoutSeconds(90)
                .retryIntervalMilliseconds(5000) // 1.25 seconds retry interval
                .maxDelayMilliseconds(15000) // 15 seconds max delay
                .build();

        reconService.updateMainStagingStatus(rideEvtID,"consumer_geolocation_process");
        geocoderService.setBlankGeoValues(event, rideEvtID, backoffConfig)
                .thenApply(geoloc -> { if (geoloc != null) logger.info("Geolocation received Successfully: " + geoloc); return geoloc; })
                .thenAccept(geoloc -> rideAdapterService.sendData(List.of(geoloc), rideEvtID, "gis", "geolocations", primaryKey.orElse(null), 5000))
                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), rideEvtID, "etk", "events", evtprimaryKey.orElse(null), 5000));
//        geocoderService.callGeocoderApi(event, rideEvtID, backoffConfig)
//                .thenApply(geoloc -> { if (geoloc != null) logger.info("Geolocation received Successfully: " + geoloc); return geoloc; })
//                .thenAccept(geoloc -> rideAdapterService.sendData(List.of(geoloc), rideEvtID, "gis", "geolocations", primaryKey.orElse(null), 5000))
//                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), rideEvtID, "etk", "events", evtprimaryKey.orElse(null), 5000));

    }
}
