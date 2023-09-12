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

@Slf4j
@ApplicationScoped
public class DisputeEvent extends EtkEventHandler<String, DisputeRecord> {

    private static final Logger logger = Logger.getLogger(DisputeEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @ConfigProperty(name = "ride.adapter.primarykey.dispute")
    Optional<List<String>> primaryKey;

    @Override
    public void execute(DisputeRecord event) {
        logger.info("Dispute Event received: " + event);
        rideAdapterService.sendData(List.of(event), "etk","disputes", primaryKey.orElse(null));
    }
}
