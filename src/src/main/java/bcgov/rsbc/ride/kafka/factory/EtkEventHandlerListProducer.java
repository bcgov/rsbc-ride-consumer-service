package bcgov.rsbc.ride.kafka.factory;

import bcgov.rsbc.ride.kafka.events.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class EtkEventHandlerListProducer {

    @Inject DisputeEvent disputeEvent;
    @Inject DisputeUpdateEvent disputeUpdateEvent;
    @Inject IssuanceEvent issuanceEvent;
    @Inject PaymentEvent paymentEvent;
    @Inject PaymentQueryEvent paymentQueryEvent;
    @Inject ViolationEvent violationEvent;
    @Inject ApproximateGeolocationEvent approximateGeolocationEvent;

    @Produces
    @ApplicationScoped
    public List<EtkEventHandler> produceEtkEventHandlers() {
        return List.of(
                disputeEvent,
                disputeUpdateEvent,
                issuanceEvent,
                paymentEvent,
                paymentQueryEvent,
                violationEvent,
                approximateGeolocationEvent
        );
    }
}
