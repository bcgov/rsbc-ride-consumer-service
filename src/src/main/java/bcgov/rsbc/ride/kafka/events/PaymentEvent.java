package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.PaymentRecord;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class PaymentEvent implements EtkEventHandler<String, PaymentRecord>{

    private static final Logger logger = Logger.getLogger(PaymentEvent.class);

    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Override
    public List<Class<PaymentRecord>> getEventTypeToHandler() { return List.of(PaymentRecord.class); }

    @Override
    public PaymentRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, PaymentRecord.class);
    }

    @Override
    public void execute(PaymentRecord event) {
        logger.info("Payment Event received: " + event);
        rideAdapterService.sendData(event, "etk.payments");
    }
}
