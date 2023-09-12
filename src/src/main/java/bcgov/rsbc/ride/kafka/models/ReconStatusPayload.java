package bcgov.rsbc.ride.kafka.models;

import bcgov.rsbc.ride.kafka.models.ReconStatusPayloadData;

public class ReconStatusPayload {

    private String collectionName;

    private ReconStatusPayloadData payloaddata;

    // Getters and setters
    public String getcollectionName() {
        return collectionName;
    }
    public void setcollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public ReconStatusPayloadData getpayloaddata() {
        return payloaddata;
    }
    public void setpayloaddata(ReconStatusPayloadData payloaddata) {
        this.payloaddata = payloaddata;
    }



}