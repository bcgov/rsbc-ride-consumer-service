package bcgov.rsbc.ride.kafka.service;

import bcgov.rsbc.ride.kafka.models.Geolocation;
import bcgov.rsbc.ride.kafka.models.GeolocationRequest;
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
import bcgov.rsbc.ride.kafka.service.BackoffExecution.BackoffConfig;
import bcgov.rsbc.ride.kafka.service.ReconService;

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
    public CompletionStage<Geolocation> callGeocoderApi( GeolocationRequest geolocationRequest, String eventId, BackoffConfig backoffConfig) {
        String addressRaw = geolocationRequest.getViolationHighwayDesc() + ", " + geolocationRequest.getViolationCityName();
        String businessId = geolocationRequest.getBusinessId();
        String address = cleanUpAddress(addressRaw);

        WebClient webClient = WebClient.create(vertx);
        logger.info("Calling Geocoder API with address: " + address + ", eventId: " + eventId);

        CompletableFuture<HttpResponse<Buffer>> responseFuture =
                backoffExecution.executionWithRetry(eventId,
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

            return Geolocation.builder()
                    .businessProgram("ETK")
                    .businessType("violation")
                    .businessId(businessId)
                    .latitude(dataBc.getBigDecimal("lat"))
                    .longitude(dataBc.getBigDecimal("lon"))
                    .precision(dataBc.getString("precision"))
                    .requestedAddress(jsonObject.getString("addressRaw"))
                    .submittedAddress(address)
                    .databcLong(dataBc.getBigDecimal("lon"))
                    .databcLat(dataBc.getBigDecimal("lat"))
                    .databcScore(dataBc.getInt("score"))
                    .databcPrecision(dataBc.getString("precision"))
                    .fullAddress(dataBc.getString("fullAddress"))
                    .faults(dataBc.getJSONArray("faults"))
                    .build();
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
