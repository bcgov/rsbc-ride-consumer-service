package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.models.*;
import bcgov.rsbc.ride.kafka.service.EtkConsumerService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.KafkaConsumer;
import io.smallrye.reactive.messaging.kafka.Record;
import org.codehaus.plexus.util.dag.Vertex;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.quality.Strictness;
import org.mockito.testng.MockitoSettings;
import java.io.IOException;
import java.util.Objects;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReceiveEventsTest {

    @InjectMocks
    RideEtkConsumer rideEtkConsumer;

    @Mock
    KafkaConsumer<Long, String> kafkaConsumer;

    @Mock
    EtkConsumerService etkConsumerService;

    @Mock
    Vertex vertex;

    WireMockServer wireMockServer;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        WireMock.configureFor("localhost", 9092);
        wireMockServer = new WireMockServer(9092);
        wireMockServer.start();
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testDisputeEvent() {
        mockingRideDbAdapter();
        DisputeRecord dispute = DisputeRecord.newBuilder()
                .setTicketNumber("222")
                .setCountNumber(2)
                .setDisputeActionDate("2022-01-01")
                .setDisputeTypeCode("222")
                .setCountActRegulation("333")
                .setCompressedSection("222")
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + dispute.toString());
        rideEtkConsumer.receive_dispute(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, dispute.toString(), DisputeRecord.class);
    }

    @Test
    public void testDisputeUpdateEvent() {
        mockingRideDbAdapter();
        DisputeUpdateRecord disputeUpdateRecord = DisputeUpdateRecord.newBuilder()
                .setTicketNumber("223")
                .setCountNumber(1)
                .setDisputeActionDate("222")
                .setDisputeActionCode("222")
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + disputeUpdateRecord.toString());
        rideEtkConsumer.receive_disputeupdate(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, disputeUpdateRecord.toString(), DisputeUpdateRecord.class);
    }

    @Test
    public void testIssuanceEvent() throws IOException {
        String mockFileLocation = "json/response/geolocation-response.json";
        String address = "1000 W 71st Ave, Vancouver, BC";

        mockingGeocoderSvc(address, mockFileLocation);
        mockingRideDbAdapter();

        IssuanceRecord issuance = IssuanceRecord.newBuilder()
                .setSubmitDate("2023-12-15")
                .setSentTime("222")
                .setTicketNumber("EZ02000448")
                .setDriversLicenceProvinceCode("222")
                .setPersonGenderCode("222")
                .setPersonResidenceCityName("222")
                .setPersonResidenceProvinceCode("222")
                .setYoungPersonYn("222")
                .setOffenderTypeCode("222")
                .setViolationDate("2020-09-17")
                .setViolationTime("12:12")
                .setViolationHighwayDesc("1000 W 71st Ave")
                .setViolationCityCode("VAN")
                .setViolationCityName("Vancouver")
                .setVehicleProvinceCode("BC")
                .setVehicleNsjPujCd("222")
                .setVehicleMakeCode("FORD")
                .setVehicleTypeCode("222")
                .setVehicleYear("222")
                .setAccidentYn("222")
                .setDisputeAddressText("222")
                .setCourtLocationCode("222")
                .setMreAgencyText("222")
                .setEnforcementJurisdictionCode("222")
                .setCertificateOfServiceDate("2020-12-25")
                .setCertificateOfServiceNumber("222")
                .setEViolationFormNumber("222")
                .setEviolationformnumber("222")
                .setEnforcementJurisdictionName("222")
                .setMreMinorVersionText("222")
                .setCountQuantity("222")
                .setEnforcementOfficerNumber("222")
                .setEnforcementOfficerName("222")
                .setSentDate("2023-08-15")
                .setEnforcementOrgUnitCd("222")
                .setEnforcementOrgUnitCdTxt("222")
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + issuance.toString());
        rideEtkConsumer.receive(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, issuance.toString(), IssuanceRecord.class);
    }

    @Test
    public void testPaymentEvent() {
        mockingRideDbAdapter();
        PaymentRecord payment = PaymentRecord.newBuilder()
                .setTicketNumber("223")
                .setCountNumber(1)
                .setPaymentCardType("222")
                .setPaymentTicketTypeCode("222")
                .setPaymentAmount(222)
                .setTransactionId("222")
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + payment.toString());
        rideEtkConsumer.receive_payment(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, payment.toString(), PaymentRecord.class);
    }

    @Test
    public void testPaymentQueryEvent() {
        mockingRideDbAdapter();
        PaymentQueryRecord paymentQuery = PaymentQueryRecord.newBuilder()
                .setTicketNumber("223")
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + paymentQuery.toString());
        rideEtkConsumer.receive_payquery(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, paymentQuery.toString(), PaymentQueryRecord.class);
    }

    @Test
    public void testViolationEvent() {
        mockingRideDbAdapter();
        ViolationRecord violation = ViolationRecord.newBuilder()
                .setTicketNumber("223")
                .setCountNumber(1)
                .setActCode("1234")
                .setSectionText("222")
                .setSectionDesc("222")
                .setFineAmount("222")
                .setWordingNumber(222)
                .build();
        Record<Long, String> record = Record.of(12345L, "12345" + violation.toString());
        rideEtkConsumer.receive_violations(record);
        verify(etkConsumerService, times(1)).processEtkEvents(record, violation.toString(), ViolationRecord.class);
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
                                .withBody("{\"status\":\"success\"}")));
    }

    @NotNull
    private static String getFileContent(String location) throws IOException {
        return new String(Objects.requireNonNull(ReceiveEventsTest.class.getClassLoader()
                .getResourceAsStream(location)).readAllBytes());
    }
}

