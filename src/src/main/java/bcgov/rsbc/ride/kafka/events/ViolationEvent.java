package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.*;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ViolationEvent implements EtkEventHandler<String, ViolationRecord>{

    private static final Logger logger = Logger.getLogger(ViolationEvent.class);

    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Override
    public List<Class<ViolationRecord>> getEventTypeToHandler() { return List.of(ViolationRecord.class); }

    @Override
    public ViolationRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, ViolationRecord.class);
    }

    @Override
    public void execute(ViolationRecord event) {
        logger.info("Violation Event received: " + event);
        rideAdapterService.sendData(event, "etk.violations");
    }
}
