package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.DisputeUpdateRecord;
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
public class DisputeUpdateEvent extends EtkEventHandler<String, DisputeUpdateRecord>{

    private static final Logger logger = Logger.getLogger(DisputeUpdateEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.disputeupdate")
    Optional<List<String>> primaryKey;

    @Override
    public void execute(DisputeUpdateRecord event, String eventId) {
        logger.info("Dispute Update Event received: " + event);
        reconService.updateMainStagingStatus(eventId,"consumer_process");
        rideAdapterService.sendData(List.of(event), eventId,
                "etk", "dispute_status_updates", primaryKey.orElse(null), 5000);
    }
}
