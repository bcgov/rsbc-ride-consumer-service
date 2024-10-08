package bcgov.rsbc.ride.kafka.service;

import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.factory.EtkEventFactory;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.reactive.messaging.kafka.Record;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EtkConsumerService {
    private static final Logger logger = Logger.getLogger(EtkConsumerService.class);

    @Inject
    EtkEventFactory etkEventsFactory;

    @Inject
    ReconService reconService;

    @WithSpan
    public <S, T> boolean processEtkEvents(Record<Long, String> event, S inputType, Class<T> eventType) {
        boolean eventStatus = false;

        String recordKey= String.valueOf(event.key());

        try {
            logger.debug("Payload: " + event);
            logger.debug("Type of event: " + event.getClass());
            EtkEventHandler<S, T> handler = etkEventsFactory.getHadlerByEventType(eventType);
            T evt = handler.map(inputType);
            logger.info("Kafka decoded event UID: " + recordKey);
            handler.execute(evt,recordKey);
            eventStatus=true;
            
        } catch (Exception e) {
            logger.error("Exception occurred while reading decoded event, exception details: " + e + "; " + e.getMessage());
            reconService.updateMainStagingStatus(recordKey, "consumer_error");
            reconService.sendErrorRecords(recordKey,e.getMessage(),"data_issue");
        }
        return eventStatus;
    }
}
