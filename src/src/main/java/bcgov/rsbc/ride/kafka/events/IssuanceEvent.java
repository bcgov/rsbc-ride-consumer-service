package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.EventRecord;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
import bcgov.rsbc.ride.kafka.models.ViolationCountRecord;
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
public class IssuanceEvent extends EtkEventHandler<String,IssuanceRecord> {

    private static final Logger logger = Logger.getLogger(IssuanceEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ApproximateGeolocationEvent approximateGeolocationEvent;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.issuance")
    Optional<List<String>> primaryKey;

    @ConfigProperty(name = "ride.adapter.primarykey.violations")
    Optional<List<String>> violationsPrimaryKey;

    @ConfigProperty(name = "ride.adapter.primarykey.events")
    Optional<List<String>> evtprimaryKey;

    @Override
    public void execute(IssuanceRecord event, String key) {
        String eventId = event.getEvent().getId();
        EventRecord eventRecord = event.getEvent();
        List<ViolationCountRecord> counts = event.getCounts();
        setEventId(event, eventId);
        JsonObject eventPayload = JsonObject.mapFrom(event);

        logger.info("Issuance Event received: \n" + eventPayload);

        eventPayload.remove("event");
        eventPayload.remove("counts");
        String rideEvtID=key;

        reconService.updateMainStagingStatus(rideEvtID,"consumer_process");
        rideAdapterService.sendData(List.of(eventPayload), rideEvtID, "etk", "issuances", primaryKey.orElse(null), 5000)
                .thenRun(() -> rideAdapterService.sendData(counts, rideEvtID, "etk", "violations", violationsPrimaryKey.orElse(null), 5000))
                .thenRun(() -> rideAdapterService.sendData(List.of(eventRecord), rideEvtID, "etk", "events", evtprimaryKey.orElse(null), 5000));
    }
}
