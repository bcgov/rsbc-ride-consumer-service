package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.DisputeRecord;
import bcgov.rsbc.ride.kafka.models.EventRecord;
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
public class DisputeEvent extends EtkEventHandler<String, DisputeRecord> {

    private static final Logger logger = Logger.getLogger(DisputeEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.dispute")
    Optional<List<String>> primaryKey;

    @ConfigProperty(name = "ride.adapter.primarykey.events")
    Optional<List<String>> evtprimaryKey;

    @Override
    public void execute(DisputeRecord event, String key) {
        String eventId = event.getEvent().getId();
        EventRecord eventRecord = event.getEvent();
        setEventId(event, eventId);
        JsonObject eventPayload = JsonObject.mapFrom(event);
        eventPayload.remove("event");
        String rideEvtID=key;

        logger.info("Dispute Event received: " + eventPayload);
        reconService.updateMainStagingStatus(rideEvtID,"consumer_process");
        rideAdapterService.sendData(List.of(eventPayload), rideEvtID, "etk","disputes", primaryKey.orElse(null), 5000)
                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), rideEvtID, "etk", "events", evtprimaryKey.orElse(null), 5000));
    }
}
