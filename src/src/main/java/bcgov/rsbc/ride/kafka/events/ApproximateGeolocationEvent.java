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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
    public void execute(GeolocationRequest event) {
        logger.info("GeolocationRequest Event received: " + event);

        String eventId = event.event().getId();
        EventRecord eventRecord = event.event();
        setEventId(event, eventId);
        JsonObject eventPayload = JsonObject.mapFrom(event);

        eventPayload.remove("event");

        BackoffConfig backoffConfig = BackoffConfig.builder()
                .maxRetries(3)
                .timeoutSeconds(3)
                .retryIntervalMilliseconds(1250) // 1.25 seconds retry interval
                .maxDelayMilliseconds(15000) // 15 seconds max delay
                .build();

        reconService.updateMainStagingStatus(eventId,"consumer_geolocation_process");
        geocoderService.callGeocoderApi(event, eventId, backoffConfig)
                .thenApply(geoloc -> { if (geoloc != null) logger.info("Geolocation received Successfully: " + geoloc); return geoloc; })
                .thenAccept(geoloc -> rideAdapterService.sendData(List.of(geoloc), eventId, "gis", "geolocations", primaryKey.orElse(null), 5000))
                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), eventId, "etk", "events", primaryKey.orElse(null), 5000));

    }
}
