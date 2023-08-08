package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.IssuanceRecord;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.opentelemetry.extension.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class IssuanceEvent implements EtkEventHandler<String,IssuanceRecord> {

    private static final Logger logger = Logger.getLogger(IssuanceEvent.class);

    @Inject
    CustomObjectMapper objectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    GeolocationEvent geolocationEvent;

    @Override
    public List<Class<IssuanceRecord>> getEventTypeToHandler() {
        return List.of(IssuanceRecord.class);
    }

    @Override
    @WithSpan
    public IssuanceRecord mapperEvent(String eventString) throws JsonProcessingException {
        return objectMapper.getObjectMapper().readValue(eventString, IssuanceRecord.class);
    }

    @Override
    @WithSpan
    public void execute(IssuanceRecord event) {
        logger.info("Issuance Event received: " + event);
        rideAdapterService.sendData(event, "etk.issuances")
                .thenRun(() -> geolocationEvent.execute(geolocationEvent.mapperEvent(event)));
    }
}