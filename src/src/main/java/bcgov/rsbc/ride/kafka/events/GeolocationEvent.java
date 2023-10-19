package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.converter.ConverterBCAlbersToWGS84;
import bcgov.rsbc.ride.kafka.converter.ConverterUTMToWGS84;
import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationAdapter;
import bcgov.rsbc.ride.kafka.models.EventRecord;
import bcgov.rsbc.ride.kafka.models.PreciseGeolocationAdapter;
import bcgov.rsbc.ride.kafka.models.PreciseGeolocationRecord;
import bcgov.rsbc.ride.kafka.service.ReconService;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class GeolocationEvent extends EtkEventHandler<String, PreciseGeolocationRecord>{

    private static final Logger logger = Logger.getLogger(GeolocationEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @Inject
    ConverterUTMToWGS84 converterUTMToWGS84;

    @Inject
    ConverterBCAlbersToWGS84 converterBCAlbersToWGS84;

    @Inject
    CustomObjectMapper customObjectMapper;

    @ConfigProperty(name = "ride.adapter.primarykey.geolocation")
    Optional<List<String>> primaryKey;

    @Override
    protected PreciseGeolocationRecord mapEvent(String preciseGeolocation) throws JsonProcessingException {
        PreciseGeolocationAdapter adapter = customObjectMapper.getObjectMapper().readValue(preciseGeolocation, PreciseGeolocationAdapter.class);
        String eventRecorString = customObjectMapper.getObjectMapper().writeValueAsString(adapter.getEvent());
        EventRecord eventRecord = customObjectMapper.getObjectMapper().readValue(eventRecorString, EventRecord.class);
        PreciseGeolocationRecord preciseGeolocationRecord = new PreciseGeolocationRecord();
        preciseGeolocationRecord.setTicketNumber(adapter.getTicket_number());
        preciseGeolocationRecord.setServerCode(adapter.getServer_code());
        preciseGeolocationRecord.setXValue(adapter.getX_value());
        preciseGeolocationRecord.setYValue(adapter.getY_value());
        preciseGeolocationRecord.setEvent(eventRecord);
        return preciseGeolocationRecord;
    }

    @Override
    public void execute(PreciseGeolocationRecord event) {
        String eventId = event.getEvent().getId();
        EventRecord eventRecord = event.getEvent();
        setEventId(event, eventId);
        JsonObject eventPayload = JsonObject.mapFrom(event);
        eventPayload.remove("event");

        logger.info("GeolocationRequest Event received: " + eventPayload);

        ApproximateGeolocationAdapter geolocation = event.getServerCode().equalsIgnoreCase("LMD") ?
                converterUTMToWGS84.convert(event) : converterBCAlbersToWGS84.convert(event);

        reconService.updateMainStagingStatus(eventId,"consumer_geolocation_process");
        rideAdapterService.sendData(List.of(geolocation.toJson()), eventId, "gis","geolocations", primaryKey.orElse(null), 5000)
                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), eventId, "etk", "events", primaryKey.orElse(null), 5000));
    }
}
