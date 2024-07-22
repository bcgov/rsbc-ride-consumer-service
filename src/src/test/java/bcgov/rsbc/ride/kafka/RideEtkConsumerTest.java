package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.events.ApproximateGeolocationEvent;
import bcgov.rsbc.ride.kafka.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.Record;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.mockito.MockitoAnnotations;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


@QuarkusTest
public class RideEtkConsumerTest {

    @Inject
    RideEtkConsumer rideEtkConsumer;

    @Inject
    CustomObjectMapper customObjectMapper;

    WireMockServer wireMockServer;
    AutoCloseable autoCloseable;

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        Options options = wireMockConfig().port(8082).bindAddress("127.0.0.1");
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        WireMock.configureFor(8082);
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }


    @Test
    public void testPingConsumeEtkEndpoint() {
        given()
                .when().get("/consumeetk/ping")
                .then()
                .statusCode(200)
                .body(is("pong from etk consumer"));
    }

    @Test
    public void testDisputeEvent() throws IOException {
        String eventId = "12345";
        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);

        Assertions.assertTrue(rideEtkConsumer.receive_dispute(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_dispute.json"),
                        DisputeRecord.class)));
    }

    @Test
    public void testDisputeUpdateEvent() throws IOException {
        String eventId = "12345";
        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);

        Assertions.assertTrue(rideEtkConsumer.receive_disputeupdate(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_dispute_update.json"),
                        DisputeUpdateRecord.class)));
    }

    @Test
    public void testIssuanceEvent() throws IOException {
        String eventId = "12345";
        String address = "1000 W 71st Ave, Vancouver, BC";
        String mockFileLocation = "json/response/geolocation-response.json";
        String eventString = getFileContent("json/requests/dbadapter/event_issuance.json");

        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);
        mockingGeocoderSvc(address, mockFileLocation);

        IssuanceRecord issuanceRecord = customObjectMapper.getObjectMapper().readValue(eventString, IssuanceRecord.class);

        assertThrows(Exception.class, () ->{
            ApproximateGeolocationEvent approximateGeolocationEvent = new ApproximateGeolocationEvent();
            approximateGeolocationEvent.execute(approximateGeolocationEvent.map(issuanceRecord), eventId);
        });

        Assertions.assertTrue(rideEtkConsumer.receive(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_issuance.json"),
                        IssuanceRecord.class)));
    }

    @Test
    public void testPaymentEvent() throws IOException {
        String eventId = "12345";
        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);

        Assertions.assertTrue(rideEtkConsumer.receive_payment(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_payment.json"),
                        PaymentRecord.class)));
    }

    @Test
    public void testPaymentQueryEvent() throws IOException {
        String eventId = "12345";
        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);

        Assertions.assertTrue(rideEtkConsumer.receive_payquery(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_payment_query.json"),
                        PaymentQueryRecord.class)));
    }

    @Test
    public void testViolationEvent() throws IOException {
        String eventId = "12345";
        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);

        Assertions.assertTrue(rideEtkConsumer.receive_violations(
                getLongStringRecord(eventId, getFileContent("json/requests/dbadapter/event_violations.json"),
                        ViolationRecord.class)));
    }

    @Test
    public void testPreciseGeolocationEvent() throws IOException {
        String eventId = "12345";
        String address = "1000 W 71st Ave, Vancouver, BC";
        String mockFileLocation = "json/response/geolocation-response.json";
        String jsonPreciseGeolocation = getFileContent("json/requests/dbadapter/event_geolocation.json");

        mockingRideDbAdapter();
        mockingSendErrorRecords(eventId);
        mockingReconUpdateMainStagingStatus(eventId);
        mockingGeocoderSvc(address, mockFileLocation);

        PreciseGeolocationAdapter intermediate = customObjectMapper.getObjectMapper().readValue(jsonPreciseGeolocation, PreciseGeolocationAdapter.class);
        String jsonString = customObjectMapper.getObjectMapper().writeValueAsString(intermediate.getEvent());
        EventRecord eventRecord = customObjectMapper.getObjectMapper().readValue(jsonString, EventRecord.class);

        PreciseGeolocationRecord preciseGeolocationRecord = new PreciseGeolocationRecord();
        preciseGeolocationRecord.setTicketNumber(intermediate.getTicket_number());
        preciseGeolocationRecord.setServerCode(intermediate.getServer_code());
        preciseGeolocationRecord.setXValue(intermediate.getX_value());
        preciseGeolocationRecord.setYValue(intermediate.getY_value());
        preciseGeolocationRecord.setEvent(eventRecord);

        Record<Long, String> event = Record.of(12345L, eventId + preciseGeolocationRecord);
        Assertions.assertTrue(rideEtkConsumer.receive_geolocation(event));
    }

    @NotNull
    private Record<Long, String> getLongStringRecord(String eventId, String eventObject, Class<?> aClass) throws JsonProcessingException {
        return Record.of(12345L, eventId + customObjectMapper.getObjectMapper()
                .readValue(eventObject, aClass).toString());
    }


    private void mockingGeocoderSvc(String address, String fileLocation) throws IOException {
        stubFor(
                get(urlEqualTo("/geocodersvc/api/v1/address?address="+ address))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(getFileContent(fileLocation))));
    }

    private void mockingRideDbAdapter() {
        stubFor(
                post(urlEqualTo("/upsertdata"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"status\":\"Recon sendData finished\"}")));
    }
    private void mockingReconUpdateMainStagingStatus(String eventId) {
        stubFor(
                patch(urlEqualTo("/updateevent/" + eventId))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"status\":\"Recon ReconUpdateMainStagingStatus finished\"}")));
    }

    private void mockingSendErrorRecords(String eventId) {
        stubFor(
                get(urlEqualTo("/querytable?collection_name=mainstaging&eventid="+eventId))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"status\":\"Recon sendErrorRecords finished\"}")));
    }

    @NotNull
    private static String getFileContent(String location) throws IOException {
        return new String(Objects.requireNonNull(RideEtkConsumerTest.class.getClassLoader()
                .getResourceAsStream(location)).readAllBytes());
    }
}