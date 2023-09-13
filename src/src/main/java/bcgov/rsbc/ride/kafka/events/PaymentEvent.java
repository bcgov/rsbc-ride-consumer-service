package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.PaymentRecord;
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
public class PaymentEvent extends EtkEventHandler<String, PaymentRecord>{

    private static final Logger logger = Logger.getLogger(PaymentEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @ConfigProperty(name = "ride.adapter.primarykey.payment")
    Optional<List<String>> primaryKey;

    @Override
    public void execute(PaymentRecord event, String eventId) {
<<<<<<< HEAD
=======
        setEventId(event, eventId);
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
        logger.info("Payment Event received: " + event);
        reconService.updateMainStagingStatus(eventId,"consumer_process");
        rideAdapterService.sendData(List.of(event), eventId,
                "etk", "payments", primaryKey.orElse(null), 5000);
    }
}
