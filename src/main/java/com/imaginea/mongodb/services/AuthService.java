package com.imaginea.mongodb.services;

import com.imaginea.mongodb.domain.ConnectionDetails;
import com.imaginea.mongodb.domain.MongoConnectionDetails;
import com.imaginea.mongodb.exceptions.ApplicationException;
import com.mongodb.Mongo;

import java.util.Set;

/**
 * @author Uday Shankar
 */
public interface AuthService {

    String authenticate(ConnectionDetails connectionDetails, Set<String> existingConnectionIdsInSession) throws ApplicationException;

    MongoConnectionDetails getMongoConnectionDetails(String connectionId) throws ApplicationException;

    Mongo getMongoInstance(String connectionId) throws ApplicationException;

    void disconnectConnection(String connectionId) throws ApplicationException;
}
