package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.DisputeRecord;
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
public class DisputeEvent extends EtkEventHandler<String, DisputeRecord> {

    private static final Logger logger = Logger.getLogger(DisputeEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.dispute")
    Optional<List<String>> primaryKey;

    @Override
    public void execute(DisputeRecord event, String eventId) {
<<<<<<< HEAD
=======
        setEventId(event, eventId);
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
        logger.info("Dispute Event received: " + event);
        reconService.updateMainStagingStatus(eventId,"consumer_process");
        rideAdapterService.sendData(List.of(event), eventId,
                "etk","disputes", primaryKey.orElse(null), 5000);
    }
}
