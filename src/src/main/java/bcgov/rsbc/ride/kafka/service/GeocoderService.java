package bcgov.rsbc.ride.kafka.service;

import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationRecord;
import bcgov.rsbc.ride.kafka.models.GeolocationRequest;
import bcgov.rsbc.ride.kafka.service.BackoffExecution.BackoffConfig;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Base64.getEncoder;

@Slf4j
@ApplicationScoped
public class GeocoderService {

    private static final Logger logger = Logger.getLogger(GeocoderService.class);

    @ConfigProperty(name = "geocoder.security.username")
    private String USERNAME;
    @ConfigProperty(name = "geocoder.security.password")
    private String PASSWORD;
    @ConfigProperty(name = "geocoder.api.port")
    private Integer PORT;
    @ConfigProperty(name = "geocoder.api.host")
    private String HOST;
    @ConfigProperty(name = "geocoder.api.uri")
    private String URI;

    @Inject
    Vertx vertx;

    @Inject
    BackoffExecution backoffExecution;

    @Inject
    ReconService reconService;


    @WithSpan
    public CompletionStage<ApproximateGeolocationRecord> setBlankGeoValues(GeolocationRequest geolocationRequest, String eventId, BackoffConfig backoffConfig) {
        String addressRaw = geolocationRequest.violationHighwayDesc() + ", " + geolocationRequest.violationCityName();
        String businessId = geolocationRequest.businessId();
        String address = cleanUpAddress(addressRaw);
        CompletableFuture<HttpResponse<Buffer>> responseFuture = new CompletableFuture<>();

        responseFuture.complete(null);

        return responseFuture.thenApply(resp -> {
//            if (resp.statusCode() != 200) {
//                logger.error("Error calling Geocoder API: " + resp.statusCode() + " " + resp.statusMessage());
//                reconService.updateMainStagingStatus(eventId,"geocoder_error");
//                reconService.sendErrorRecords(eventId,"error in getting geolocation","others");
//                return null;
//            }
//            JSONObject jsonObject = new JSONObject(resp.bodyAsString());
//            JSONObject dataBc = jsonObject.getJSONObject("dataBc");

            try {
                ApproximateGeolocationRecord geolocation =  new ApproximateGeolocationRecord();
                geolocation.setBusinessProgram("ETK");
                geolocation.setBusinessType("violation");
                geolocation.setBusinessId(businessId);
                geolocation.setLat("PENDING");
                geolocation.setLong$("PENDING");
                geolocation.setPrecision("");
                geolocation.setRequestedAddress("");
                geolocation.setSubmittedAddress(address);
                geolocation.setDatabcLong("");
                geolocation.setDatabcLat("");
                geolocation.setDatabcScore("");
                geolocation.setDatabcPrecision("");
                geolocation.setFullAddress("");
                geolocation.setFaults("");

//                logger.info("Successful callGeocoderApi returning: " + geolocation + ", eventId: " + eventId);
                logger.info("Successful set blank values for  " + geolocation + ", eventId: " + eventId);

//                convert geolocation to responseFuture
//                responseFuture.complete(resp);


//                geolocation=new responseFuture(geolocation);

                return geolocation;
            }
            catch (Exception e) {
                logger.error("Error in converting geolocation to json: " + e.getMessage());
                return null;
            }
        }).exceptionally(e -> {
            reconService.updateMainStagingStatus(eventId,"geocoder_error");
            reconService.sendErrorRecords(eventId,"error in setting blank geolocation","others");
            throw new RuntimeException("Error in setting blank lat-long: " + e.getMessage());
        });

    }


    @WithSpan
    public CompletionStage<ApproximateGeolocationRecord> callGeocoderApi(GeolocationRequest geolocationRequest, String eventId, BackoffConfig backoffConfig) {
        String addressRaw = geolocationRequest.violationHighwayDesc() + ", " + geolocationRequest.violationCityName();
        String businessId = geolocationRequest.businessId();
        String address = cleanUpAddress(addressRaw);

        WebClient webClient = WebClient.create(vertx);
        logger.info("Calling Geocoder API with address: " + address + ", eventId: " + eventId);

        CompletableFuture<HttpResponse<Buffer>> responseFuture =
                backoffExecution.executionWithRetry("callGeocoderApi for event id " + eventId,
                        () -> webClient.get(PORT, HOST, URI)
                                .putHeader("Content-Type", "application/json")
                                .putHeader("Authorization", "Basic " + getEncodedCredentials())
                                .setQueryParam("address", address)
                                .send().toCompletionStage()
                                .toCompletableFuture(), backoffConfig);

        return responseFuture.thenApply(resp -> {
            if (resp.statusCode() != 200) {
                logger.error("Error calling Geocoder API: " + resp.statusCode() + " " + resp.statusMessage());
                reconService.updateMainStagingStatus(eventId,"geocoder_error");
                reconService.sendErrorRecords(eventId,"error in getting geolocation","others");
                return null;
            }
            JSONObject jsonObject = new JSONObject(resp.bodyAsString());
            JSONObject dataBc = jsonObject.getJSONObject("dataBc");

            try {
                ApproximateGeolocationRecord geolocation =  new ApproximateGeolocationRecord();
                geolocation.setBusinessProgram("ETK");
                geolocation.setBusinessType("violation");
                geolocation.setBusinessId(businessId);
                geolocation.setLat(dataBc.getBigDecimal("lat").toString());
                geolocation.setLong$(dataBc.getBigDecimal("lon").toString());
                geolocation.setPrecision(dataBc.getString("precision"));
                geolocation.setRequestedAddress(jsonObject.getString("addressRaw"));
                geolocation.setSubmittedAddress(address);
                geolocation.setDatabcLong(dataBc.getBigDecimal("lon").toString());
                geolocation.setDatabcLat(dataBc.getBigDecimal("lat").toString());
                geolocation.setDatabcScore(String.valueOf(dataBc.getInt("score")));
                geolocation.setDatabcPrecision(dataBc.getString("precision"));
                geolocation.setFullAddress(dataBc.getString("fullAddress"));
                geolocation.setFaults(dataBc.getJSONArray("faults").toString());

                logger.info("Successful callGeocoderApi returning: " + geolocation + ", eventId: " + eventId);

                return geolocation;
            }
            catch (Exception e) {
                logger.error("Error in converting geolocation to json: " + e.getMessage());
                return null;
            }
        }).exceptionally(e -> {
            reconService.updateMainStagingStatus(eventId,"geocoder_error");
            reconService.sendErrorRecords(eventId,"error in getting geolocation","others");
            throw new RuntimeException("Error calling Geocoder API: " + e.getMessage());
        });
    }

    public String getEncodedCredentials() {
        String credentials = USERNAME + ":" + PASSWORD;
        return getEncoder().encodeToString(credentials.getBytes());
    }

    public String cleanUpAddress(String address) {
        // make a copy so we don't change the original
        logger.info("raw address " + address);

        // ---- delete text that confuses the geocoder ----
        address = address
                .replace("\r\n", "\n")
                .replaceAll("(\\s|^|\\()[NEWS](/)?B(,|\\))?(\\s)", " ")
                .replaceAll("(\\s|^|\\()(NORTH|EAST|SOUTH|WEST)(\\s)?BOUND(,)?(\\s)", "\\1")
                .replace("#", " ");

        // move block number to beginning of address string
        String regEx = ",?(\\s|\\()?([0-9]+)(\\s)?(BLK|BLOCK)(\\))?,?(\\s+)?(OF\\s+)?";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(address);
        if (matcher.find()) {
            address = address.replaceAll(regEx, " ");
            address = matcher.group(2) + " " + address;
            address = address.replaceFirst("\\s+$", "");
        }

        // ---- replace text that indicates an intersection of two roads
        address = address
                .replaceAll(",\\s(NORTH|EAST|SOUTH|WEST),\\s+", " AND ")
                .replaceAll("\\s+NEAR\\s+", " AND ")
                .replaceAll("&amp;", " AND ")
                .replaceAll("\\s+S/O\\s+", " AND ")
                .replaceAll("\\s+SOUTH OF\\s+", " AND ")
                .replaceAll("\\s+N/O\\s+", " AND ")
                .replaceAll("\\s+NORTH OF\\s+", " AND ")
                .replaceAll("\\s+W/O\\s+", " AND ")
                .replaceAll("\\s+WEST OF\\s+", " AND ")
                .replaceAll("\\s+E/O\\s+", " AND ")
                .replaceAll("\\s+EAST OF\\s+", " AND ")
                .replaceAll("(\\s|^)(ON|OFF)(\\s)?RAMP\\s(TO|FROM)(,)?(\\s)", " AND ")
                .replace("/", " AND ")
                .replace("+", " AND ")
                .replace("@", " AND ")
                .replaceAll(" AT ", " AND ")

                .replaceAll("\\s+-\\s+", " AND ")

                .replace("HIGHWAY", "HWY")
                .replace("TRANS CANADA HWY", "TRANS-CANADA HWY")
                .replace("PAT BAY HWY", "PATRICIA BAY HWY")
                .replaceAll("HWY\\s+1([\\s+|,])", "TRANS-CANADA HWY\\1")
                .replaceAll("HWY\\sONE([\\s+|,])", "TRANS-CANADA HWY\\1")
                .replaceAll("HWY\\s?(\\d+)", "HWY-$1")
                .replaceAll("HWY-(\\d+)(\\s?)(SOUTH|NORTH|EAST|WEST|[NEWS])(\\s|,)", "HWY-$1 ")
                .replaceAll("[^\\S\\r\\n]{2,}", " ")
                .replaceAll("^\\s+", "");

        address = address + ", BC";

        logger.info("clean address " + address);
        return address;
    }

}
