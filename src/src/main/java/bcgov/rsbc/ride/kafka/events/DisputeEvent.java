package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.DisputeRecord;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class DisputeEvent implements EtkEventHandler<String, DisputeRecord>{

    private static final Logger logger = Logger.getLogger(DisputeEvent.class);

    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Override
    public List<Class<DisputeRecord>> getEventTypeToHandler() { return List.of(DisputeRecord.class); }

    @Override
    public DisputeRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, DisputeRecord.class);
    }

    @Override
    public void execute(DisputeRecord event) {
        logger.info("Dispute Event received: " + event);
        rideAdapterService.sendData(event, "etks.disputes");
    }
}
