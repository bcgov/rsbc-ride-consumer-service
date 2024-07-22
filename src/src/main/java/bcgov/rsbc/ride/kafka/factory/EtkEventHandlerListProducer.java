package bcgov.rsbc.ride.kafka.factory;

import bcgov.rsbc.ride.kafka.events.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class EtkEventHandlerListProducer {

    @Inject DisputeEvent disputeEvent;
    @Inject DisputeUpdateEvent disputeUpdateEvent;
    @Inject IssuanceEvent issuanceEvent;
    @Inject PaymentEvent paymentEvent;
    @Inject PaymentQueryEvent paymentQueryEvent;
    @Inject ViolationEvent violationEvent;
    @Inject GeolocationEvent geolocationEvent;
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
                geolocationEvent,
                approximateGeolocationEvent
        );
    }
}
