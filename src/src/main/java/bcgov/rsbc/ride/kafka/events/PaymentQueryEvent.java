package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.PaymentQueryRecord;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class PaymentQueryEvent implements EtkEventHandler<String, PaymentQueryRecord>{

    private static final Logger logger = Logger.getLogger(PaymentQueryEvent.class);

    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Override
    public List<Class<PaymentQueryRecord>> getEventTypeToHandler() { return List.of(PaymentQueryRecord.class); }

    @Override
    public PaymentQueryRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, PaymentQueryRecord.class);
    }

    @Override
    public void execute(PaymentQueryRecord event) {
        logger.info("Payment Query Event received: " + event);
        rideAdapterService.sendData(event, "etk.queries");
    }
}
