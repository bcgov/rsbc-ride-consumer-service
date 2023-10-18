package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
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

    @Override
    public void execute(IssuanceRecord event, String eventId) {
        setEventId(event, eventId);
        logger.info("Issuance Event received: " + event);
        reconService.updateMainStagingStatus(eventId,"consumer_process");
        rideAdapterService.sendData(List.of(event), eventId, "etk", "issuances", primaryKey.orElse(null), 5000)
                .thenRun(() -> rideAdapterService.sendData(List.of(event.getCounts()), eventId, "etk", "violations", primaryKey.orElse(null), 5000))
                .thenRun(() -> approximateGeolocationEvent.execute(approximateGeolocationEvent.map(event), eventId))
                .thenRun(() -> rideAdapterService.sendData(List.of(event.getEvent()), eventId, "etk", "events", primaryKey.orElse(null), 5000));
    }
}
