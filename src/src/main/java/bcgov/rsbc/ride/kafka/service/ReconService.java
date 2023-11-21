package bcgov.rsbc.ride.kafka.service;

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
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class ReconService {

    private static final Logger logger = Logger.getLogger(ReconService.class);

    @ConfigProperty(name = "ride.reconsvc.api.port")
    private Integer PORT;

    @ConfigProperty(name = "ride.reconsvc.api.host")
    private String HOST;

    @Inject
    Vertx vertx;


    public CompletionStage<HttpResponse<Buffer>> updateMainStagingStatus(String eventId,String statusVal) {

        WebClient webClient = WebClient.create(vertx);
        JSONObject payload = getStatusUpdPayload(eventId,statusVal);
        logger.info("Calling Recon service status update for eventId " + eventId + " with payload: \n" + payload);
//        concatenate eventid to string
        String apiPath="/updateevent/"+eventId;

        return webClient.patch(PORT, HOST, apiPath)
                .putHeader("Content-Type", "application/json")
                .sendJson(payload).toCompletionStage().thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                        logger.error("Error calling Recon Svc API: " + resp.statusCode() + " " + resp.statusMessage());
                        return null;
                    }
                    logger.info("Updating Recon staging finished.");
                    return resp;
                });
    }

    private JSONObject getStatusUpdPayload(String eventId,String statusVal) {
        JSONObject payloadobj = new JSONObject();
        payloadobj.put("messageStatus",statusVal);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionName", "mainstaging");
        jsonObject.put("payloaddata", payloadobj);
        return jsonObject;
    }


//    send error records to recon api
    public CompletionStage<HttpResponse<Buffer>> sendErrorRecords(String eventId,String errMsg,String errCode) {
        logger.info("Sending error records to Recon service for eventid: " + eventId);
        //DONE: Query the mainstaging collection for the eventid and get payload data and endpoints
        String apiPath="/querytable?collection_name=mainstaging&eventid="+eventId;
        logger.info("Calling Recon service query table with apiPath: " + apiPath);
        WebClient webClient = WebClient.create(vertx);
        return webClient.get(PORT, HOST, apiPath)
                .putHeader("Content-Type", "application/json")
                .send().toCompletionStage().thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                        logger.error("Error calling Recon Query Svc API: " + resp.statusCode() + " " + resp.statusMessage());
                        return null;
                    }
                    logger.info("Querying Recon staging finished.");
                    logger.info(resp.body());
                    List respList = resp.body().toJsonArray().getList();
                    if (respList.size() == 0) {
                        logger.error("No event records found for eventid: " + eventId);
                        return null;
                    }
                    logger.info(respList.get(0));
                    JSONObject jsonObject = new JSONObject((Map) respList.get(0));
                    logger.info(jsonObject.get("payloaddata"));
                    JSONObject tmpjsonObject = new JSONObject((Map) jsonObject.get("payloaddata"));

                    JSONObject errPayload = new JSONObject();
                    errPayload.put("apipath",jsonObject.get("apipath"));
                    errPayload.put("payloaddata",tmpjsonObject.toJSONString());
                    errPayload.put("datasource",jsonObject.get("datasource"));
                    errPayload.put("eventid",jsonObject.get("eventid"));
                    errPayload.put("errorReason",errMsg);
                    errPayload.put("errorType","consumer_svc");
                    String[] tmpArr = jsonObject.get("apipath").toString().split("/");
                    errPayload.put("eventType",tmpArr[2]);
                    errPayload.put("messageStatus","consumer_error");
                    errPayload.put("errorcategory",errCode);

                    String insrtapiPath="/saveerrorstaging";
                    logger.info("Calling Recon service save error staging with apiPath: " + insrtapiPath);
                    webClient.post(PORT, HOST, insrtapiPath)
                            .putHeader("Content-Type", "application/json")
                            .sendJson(errPayload).toCompletionStage().thenApply(resp2 -> {
                                if (resp2.statusCode() != 200) {
                                    logger.error("Error calling Recon Save Error Svc API: " + resp2.statusCode() + " " + resp2.statusMessage());
                                    return null;
                                }
                                logger.info("Saving Recon error staging finished.");
                                logger.info(resp2.body());
                                return resp2;
                            });

            return resp;
        });
//        DONE: Call the recon api send error endpoint to send the error records
    }
}