package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.DisputeUpdateRecord;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class DisputeUpdateEvent implements EtkEventHandler<String, DisputeUpdateRecord>{

    private static final Logger logger = Logger.getLogger(DisputeUpdateEvent.class);


    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Override
    public List<Class<DisputeUpdateRecord>> getEventTypeToHandler() { return List.of(DisputeUpdateRecord.class); }

    @Override
    public DisputeUpdateRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, DisputeUpdateRecord.class);
    }

    @Override
    public void execute(DisputeUpdateRecord event) {
        logger.info("Dispute Update Event received: " + event);
        rideAdapterService.sendData(event, "etk.dispute_status_update");
    }
}
