package com.imaginea.mongodb.services;

import com.imaginea.mongodb.domain.ConnectionDetails;
import com.imaginea.mongodb.domain.MongoConnectionDetails;
import com.imaginea.mongodb.exceptions.ApplicationException;
import com.mongodb.Mongo;

/**
 * @author Uday Shankar
 */
public interface AuthService {

    String authenticate(ConnectionDetails connectionDetails) throws ApplicationException;

    MongoConnectionDetails getMongoConnectionDetails(String connectionId) throws ApplicationException;

    Mongo getMongoInstance(String connectionId) throws ApplicationException;

    void disconnectConnection(String connectionId) throws ApplicationException;

    boolean doesConnectionExists(String connectionId);
}
