package com.imaginea.mongodb.domain;

import com.mongodb.Mongo;

public class MongoConnectionDetails {
    private ConnectionDetails connectionDetails;
    private Mongo mongo;
    private String connectionId;

    public MongoConnectionDetails(ConnectionDetails connectionDetails, Mongo mongo, String connectionId) {
        this.connectionDetails = connectionDetails;
        this.mongo = mongo;
        this.connectionId = connectionId;
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public Mongo getMongo() {
        return mongo;
    }

    public String getConnectionId() {
        return connectionId;
    }
}