package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.*;
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
public class ViolationEvent extends EtkEventHandler<String, ViolationRecord>{

    private static final Logger logger = Logger.getLogger(ViolationEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.violations")
    Optional<List<String>> primaryKey;

    @Override
    public void execute(ViolationRecord event, String key) {
        logger.info("Violation Event received: " + event);
        reconService.updateMainStagingStatus(key,"consumer_process");
        rideAdapterService.sendData(List.of(event), "etk", "violations", primaryKey.orElse(null));
    }
}
