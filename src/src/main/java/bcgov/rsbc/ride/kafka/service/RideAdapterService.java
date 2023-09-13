package bcgov.rsbc.ride.kafka.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
<<<<<<< HEAD

import bcgov.rsbc.ride.kafka.service.ReconService;
=======
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)

@Slf4j
@ApplicationScoped
public class RideAdapterService {

    private static final Logger logger = Logger.getLogger(RideAdapterService.class);

    @ConfigProperty(name = "ride.adapter.api.port")
    private Integer PORT;
    @ConfigProperty(name = "ride.adapter.api.host")
    private String HOST;

    @Inject
    ReconService reconService;

    @Inject
    Vertx vertx;


    @WithSpan
    public CompletableFuture<HttpResponse<Buffer>> sendData(List<Object> persistenceList, String eventId, String schema, String tableName,
                                                            List<String> primaryKey, int timeoutMilliseconds) {

        WebClient webClient = WebClient.create(vertx);
<<<<<<< HEAD
        JSONObject payload = getPayload(persistenceList, eventId, schema, tableName, primaryKey);
        logger.info("Calling Ride DB Adapter API with payload: " + payload + ", eventId: " + eventId);
=======
        String payload = getPayload(persistenceList, eventId, schema, tableName, primaryKey);
        logger.info("Calling Ride DB Adapter API. EventId: " + eventId + ", payload: " + payload);
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)

        return webClient
                .post(PORT, HOST,"/upsertdata")
                .timeout(timeoutMilliseconds)
                .putHeader("Content-Type", "application/json")
                .sendJson(payload)
                .toCompletionStage()
                .exceptionally(e -> {
                    logger.error("Error calling Ride DB Adapter API: " + e.getMessage() + ", eventId:" + eventId);
<<<<<<< HEAD
                    reconService.updateMainStagingStatus(eventId,"consumer_error");
                    reconService.sendErrorRecords(eventId,"Error calling Ride DB Adapter API: " + e.getMessage(),"others");
=======
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
                    return null;
                })
                .toCompletableFuture()
                .thenApply(resp -> {
                    if (resp.statusCode() != 200) {
<<<<<<< HEAD
                       logger.error("Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage() + ", eventId:" + eventId);
                        reconService.updateMainStagingStatus(eventId,"consumer_error");
                        reconService.sendErrorRecords(eventId,"Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage(),"others");
                        return null;
                    }
                    logger.info("Persistence finished" + ", eventId:" + eventId);
                    reconService.updateMainStagingStatus(eventId,"consumer_bi_sent");
=======
                        logger.error("Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage() + ", eventId:" + eventId);
                        return null;
                    }
                    logger.info("Persistence finished" + ", eventId:" + eventId);
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
                    return resp;
                });
    }

<<<<<<< HEAD
    private JSONObject getPayload(List<Object> persistenceList, String eventId, String schema, String tableName, List<String> primaryKey) {
=======
    private String getPayload(List<Object> persistenceList, String eventId, String schema, String tableName, List<String> primaryKey) {
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tablename", tableName);
        jsonObject.put("schema", schema);
        jsonObject.put("destination", "bi");
        jsonObject.put("data", persistenceList);
        jsonObject.put("source", "etk_consumer");
<<<<<<< HEAD
        jsonObject.put("eventid", eventId);
        if (primaryKey != null) {
            jsonObject.put("primarykeys", primaryKey);
        }
        return jsonObject;
=======
        jsonObject.put("primarykeys", primaryKey != null ? primaryKey : "NA");
        return jsonObject.toJSONString();
>>>>>>> 9982d73 (Fix Dispute updates table name and avros)
    }
}
