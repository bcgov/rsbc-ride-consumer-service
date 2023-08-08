package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.models.*;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.kafka.Record;
import org.jboss.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import bcgov.rsbc.ride.kafka.service.EtkConsumerService;


@RequestScoped
@Path("/consumeetk")
public class RideEtkConsumer {
    private static final Logger logger = Logger.getLogger(RideEtkConsumer.class);

    private final Tracer tracer = GlobalOpenTelemetry.getTracer(
            RideEtkConsumer.class.getName(), "1.0.0");

    @Inject
    private EtkConsumerService etkConsumerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String etkping() {
        return "pong from etk consumer";
    }


    // Monitors the incoming-issuance topic
    @Incoming("incoming-issuance")
    @Blocking
    public Boolean receive(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive.incoming-issuance").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue,IssuanceRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while reading decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }

    @Incoming("incoming-payment")
    @Blocking
    public Boolean receive_payment(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive_payment.incoming-payment").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue, PaymentRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }

    @Incoming("incoming-dispute")
    @Blocking
    public Boolean receive_dispute(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive_dispute.incoming-dispute").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue, DisputeRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }

    @Incoming("incoming-disputeupdate")
    @Blocking
    public Boolean receive_disputeupdate(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive_disputeupdate.incoming-disputeupdate").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue, DisputeUpdateRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }

    @Incoming("incoming-violations")
    @Blocking
    public Boolean receive_violations(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive_violations.incoming-violations").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue, ViolationRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }

    @Incoming("incoming-payquery")
    @Blocking
    public Boolean receive_payquery(Record<Long, String> event) {
        Span span = tracer.spanBuilder("RideEtkConsumer.receive_payquery.incoming-payquery").startSpan();
        boolean eventStatus = false;
        try (Scope scope = span.makeCurrent()){
            String recordValue = event.value().substring(5);
            eventStatus=etkConsumerService.processEtkEvents(event, recordValue, PaymentQueryRecord.class);
            if(!eventStatus){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: " + e + "; " + e.getMessage());
        } finally {
            span.end();
        }
        return eventStatus;
    }
}