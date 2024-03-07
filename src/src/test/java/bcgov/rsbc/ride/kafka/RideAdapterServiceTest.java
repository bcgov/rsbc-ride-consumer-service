package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.models.*;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.Record;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@QuarkusTest
public class RideAdapterServiceTest {

    @Inject
    CustomObjectMapper customObjectMapper;

    @Inject
    RideAdapterService rideAdapterService;

    @Test
    public void testDisputeEvent() throws IOException {
        String eventId = "12345";
        DisputeRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_dispute.json"), DisputeRecord.class);
        event.setEventId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "disputes", null);
        Assertions.assertEquals(getFileContent("json/requests/payload/dispute.json"), payload.toJSONString());
    }

    @Test
    public void testDisputeUpdateEvent() throws IOException {
        String eventId = "12345";
        DisputeUpdateRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_dispute_update.json"), DisputeUpdateRecord.class);
        event.setEventId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "dispute_status_updates", null);
        Assertions.assertEquals(getFileContent("json/requests/payload/dispute_update.json"), payload.toJSONString());
    }

    @Test
    public void testIssuanceEvent() throws IOException {
        String eventId = "12345";
        IssuanceRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_issuance.json"), IssuanceRecord.class);
        event.setEventId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "issuances", null);
        Assertions.assertEquals(getFileContent("json/requests/payload/issuances.json"), payload.toJSONString());
    }

    @Test
    public void testPaymentEvent() throws IOException {
        String eventId = "12345";
        PaymentRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_payment.json"), PaymentRecord.class);
        event.setEventId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "payments", null);
        System.out.println(payload.toJSONString());
        Assertions.assertEquals(getFileContent("json/requests/payload/payment.json"), payload.toJSONString());
    }

    @Test
    public void testPaymentQueryEvent() throws IOException {
        String eventId = "12345";
        PaymentQueryRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_payment_query.json"), PaymentQueryRecord.class);
        event.setEventId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "queries", null);
        System.out.println(payload.toJSONString());
        Assertions.assertEquals(getFileContent("json/requests/payload/querie.json"), payload.toJSONString());
    }

    @Test
    public void testViolationEvent() throws IOException {
        String eventId = "12345";
        ViolationRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_violations.json"), ViolationRecord.class);
        event.getEvent().setId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "etk", "violations", null);
        System.out.println(payload.toJSONString());
        Assertions.assertEquals(getFileContent("json/requests/payload/violation.json"), payload.toJSONString());
    }

    @Test
    public void testPreciseGeolocationEvent() throws IOException {
        String eventId = "12345";
        PreciseGeolocationRecord event = customObjectMapper.getObjectMapper().readValue(getFileContent("json/requests/dbadapter/event_geolocation.json"), PreciseGeolocationRecord.class);
        event.getEvent().setId(eventId);
        JSONObject payload = rideAdapterService.getPayload(List.of(event), eventId, "gis", "geolocations", null);
        System.out.println(payload.toJSONString());
        Assertions.assertEquals(getFileContent("json/requests/payload/geolocation.json"), payload.toJSONString());
    }

    @NotNull
    private Record<Long, String> getLongStringRecord(String eventId, String eventObject, Class<?> aClass) throws JsonProcessingException {
        return Record.of(12345L, eventId + customObjectMapper.getObjectMapper()
                .readValue(eventObject, aClass).toString());
    }

    @NotNull
    private static String getFileContent(String location) throws IOException {
        return new String(Objects.requireNonNull(RideAdapterServiceTest.class.getClassLoader()
                .getResourceAsStream(location)).readAllBytes());
    }
}