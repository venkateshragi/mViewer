/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.imaginea.mongodb.services.impl;

import com.imaginea.mongodb.services.DocumentService;
import com.imaginea.mongodb.utils.MongoInstanceProvider;
import com.imaginea.mongodb.utils.SessionMongoInstanceProvider;
import com.imaginea.mongodb.exceptions.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Defines services definitions for performing operations like
 * create/update/delete on documents inside a collection in a database present
 * in mongo to which we are connected to. Also provides service to get list of
 * all documents present.
 *
 * @author Srinath Anantha
 */

public class DocumentServiceImpl implements DocumentService {
    /**
     * Instance variable used to get a mongo instance after binding to an
     * implementation.
     */
    private MongoInstanceProvider mongoInstanceProvider;
    /**
     * Mongo Instance to communicate with mongo
     */
    private Mongo mongoInstance;

    /**
     * Creates an instance of MongoInstanceProvider which is used to get a mongo
     * instance to perform operations on documents. The instance is created
     * based on a userMappingKey which is recieved from the database request
     * dispatcher and is obtained from tokenId of user.
     *
     * @param dbInfo A combination of username,mongoHost and mongoPort
     */
    public DocumentServiceImpl(String dbInfo) {
        mongoInstanceProvider = new SessionMongoInstanceProvider(dbInfo);
    }

    /**
     * Gets the list of documents inside a collection in a database in mongo to
     * which user is connected to.
     *
     * @param dbName         Name of Database
     * @param collectionName Name of Collection from which to get all Documents
     * @param query          query to be performed. In case of empty query {} return all
     *                       docs.
     * @param keys           Keys to be present in the resulted docs.
     * @param limit          Number of docs to show.
     * @param skip           Docs to skip from the front.
     * @return List of all documents.
     * @throws DatabaseException   throw super type of UndefinedDatabaseException
     * @throws ValidationException throw super type of
     *                             EmptyDatabaseNameException,EmptyCollectionNameException
     * @throws CollectionException throw super type of UndefinedCollectionException
     * @throws DocumentException   exception while performing get doc list
     */

    public JSONObject getQueriedDocsList(String dbName, String collectionName, DBObject query, DBObject keys, int limit, int skip) throws DatabaseException, CollectionException,
        DocumentException, ValidationException {

        mongoInstance = mongoInstanceProvider.getMongoInstance();

        if (dbName == null) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");

        }
        if (dbName.equals("")) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
        }

        if (collectionName == null) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
        }
        if (collectionName.equals("")) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection Name Empty");
        }

        JSONObject result = new JSONObject();
        try {
            if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB with name [" + dbName + "]DOES_NOT_EXIST");
            }

            if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
                throw new CollectionException(ErrorCodes.COLLECTION_DOES_NOT_EXIST, "Collection with name [" + collectionName + "] DOES NOT EXIST in Database [" + dbName + "]");
            }
            if (keys.keySet().isEmpty()) {
                keys.put("_id", 1); // For empty keys return all _id of all docs
            }

            // Return Queried Documents
            DBCursor cursor = mongoInstance.getDB(dbName).getCollection(collectionName).find(query, keys);
            cursor.limit(limit);
            cursor.skip(skip);

            ArrayList<DBObject> dataList = new ArrayList<DBObject>();
            if (cursor.hasNext()) {
                while (cursor.hasNext()) {
                    dataList.add(cursor.next());
                }
            }
            long count = mongoInstance.getDB(dbName).getCollection(collectionName).count(query);
            result.put("documents", dataList);
            result.put("count", count);
        } catch (MongoException e) {
            throw new DocumentException(ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION, e.getMessage());
        } catch (JSONException e) {
            throw new DocumentException(ErrorCodes.JSON_EXCEPTION, e.getMessage());
        }
        return result;
    }

    /**
     * Insert a document inside a collection in a database in mongo to which
     * user is connected to.
     *
     * @param dbName         Name of Database
     * @param collectionName Name of Collection from which to get all Documents
     * @param document       : Document data to be inserted
     * @return : Insertion Status
     * @throws DatabaseException   throw super type of UndefinedDatabaseException
     * @throws ValidationException throw super type of
     *                             EmptyDatabaseNameException,EmptyCollectionNameException
     *                             ,EmptyDocumentDataException
     * @throws CollectionException throw super type of UndefinedCollectionException
     * @throws DocumentException   throw super type of InsertDocumentException
     */

    public String insertDocument(String dbName, String collectionName, DBObject document) throws DatabaseException, CollectionException, DocumentException, ValidationException {
        mongoInstance = mongoInstanceProvider.getMongoInstance();
        if (dbName == null) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");

        }
        if (dbName.equals("")) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
        }

        if (collectionName == null) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
        }
        if (collectionName.equals("")) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection Name Empty");
        }

        String result = null;
        try {
            if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
            }

            if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
                throw new CollectionException(ErrorCodes.COLLECTION_DOES_NOT_EXIST, "COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
            }

            // MongoDb permits Duplicate document Insert
            mongoInstance.getDB(dbName).getCollection(collectionName).insert(document);
            result = "Inserted Document with Data : [" + document + "]";
        } catch (MongoException e) {
            throw new DocumentException(ErrorCodes.DOCUMENT_CREATION_EXCEPTION, e.getMessage());
        }
        return result;
    }

    /**
     * Updates a document inside a collection in a database in mongo to which
     * user is connected to.
     *
     * @param dbName         Name of Database
     * @param collectionName Name of Collection from which to get all Documents
     * @param _id            Id of Document to be updated
     * @param newData        new Document value.
     * @return Update status
     * @throws DatabaseException   throw super type of UndefinedDatabaseException
     * @throws ValidationException throw super type of
     *                             EmptyDatabaseNameException,EmptyCollectionNameException
     *                             ,EmptyDocumentDataException
     * @throws CollectionException throw super type of UndefinedCollectionException
     * @throws DocumentException   throw super type of UpdateDocumentException
     * @throws JSONException
     */

    public String updateDocument(String dbName, String collectionName, String _id, DBObject newData) throws DatabaseException, CollectionException, DocumentException, ValidationException {

        mongoInstance = mongoInstanceProvider.getMongoInstance();
        if (dbName == null) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");

        }
        if (dbName.equals("")) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
        }

        if (collectionName == null) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
        }
        if (collectionName.equals("")) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection Name Empty");
        }
        String result = null;
        DBObject documentData = null;
        try {
            if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
            }

            if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
                throw new CollectionException(ErrorCodes.COLLECTION_DOES_NOT_EXIST, "COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
            }
            if (_id == null) {
                throw new DocumentException(ErrorCodes.DOCUMENT_EMPTY, "Document is empty");
            }

            Object newId = newData.get("_id");
            if (newId == null) {
                throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "Object Id is invalid.");
            }
            if (newId.equals("")) {
                throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "Object Id is invalid.");
            }

            Object docId = JSON.parse(_id);
            if (!newId.equals(docId)) {
                throw new DocumentException(ErrorCodes.UPDATE_OBJECT_ID_EXCEPTION, "_id cannot be updated.");
            } else {
                // Id's equal but putting the id of old document still
                // as newData as id of string type but we need ObjectId type
                newData.put("_id", docId);
            }
            DBObject query = new BasicDBObject("_id", docId);

            DBCollection collection = mongoInstance.getDB(dbName).getCollection(collectionName);
            DBObject doc = collection.findOne(query);
            if (doc == null) {
                throw new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document does not exist !");
            }

            collection.update(doc, newData, true, false);
            documentData = collection.findOne(query);

        } catch (IllegalArgumentException e) {
            // When error converting object Id
            throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "Object Id is invalid.");

        } catch (MongoException e) {
            throw new DocumentException(ErrorCodes.DOCUMENT_UPDATE_EXCEPTION, e.getMessage());
        }
        result = "Document: [" + documentData + "] has been updated.";

        return result;
    }

    /**
     * Deletes a document inside a collection in a database in mongo to which
     * user is connected to.
     *
     * @param dbName         Name of Database
     * @param collectionName Name of Collection from which to get all Documents
     * @param _id            Id of Document to be updated
     * @return Deletion status
     * @throws DatabaseException   throw super type of UndefinedDatabaseException
     * @throws ValidationException throw super type of
     *                             EmptyDatabaseNameException,EmptyCollectionNameException
     *                             ,EmptyDocumentDataException
     * @throws CollectionException throw super type of UndefinedCollectionException
     * @throws DocumentException   throw super type of DeleteDocumentException
     */

    public String deleteDocument(String dbName, String collectionName, String _id) throws DatabaseException, CollectionException, DocumentException, ValidationException {
        mongoInstance = mongoInstanceProvider.getMongoInstance();
        if (dbName == null) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");

        }
        if (dbName.equals("")) {
            throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
        }

        if (collectionName == null) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
        }
        if (collectionName.equals("")) {
            throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection Name Empty");
        }

        String result = null;
        DBObject documentData = null;
        try {
            if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
            }

            if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
                throw new CollectionException(ErrorCodes.COLLECTION_DOES_NOT_EXIST, "COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
            }
            DBCollection coll = mongoInstance.getDB(dbName).getCollection(collectionName);
            if (coll.isCapped()) {
                throw new DocumentException(ErrorCodes.DELETING_FROM_CAPPED_COLLECTION, "Cannot Delete From a Capped Collection");
            }
            if (_id == null) {
                throw new DocumentException(ErrorCodes.DOCUMENT_EMPTY, "Document is empty");
            }

            DBObject query = new BasicDBObject();
            Object docId = JSON.parse(_id);
            query.put("_id", docId);
            DBCollection collection = this.mongoInstance.getDB(dbName).getCollection(collectionName);
            documentData = collection.findOne(query);

            if (documentData == null) {
                throw new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document does not exist !");
            }

            mongoInstance.getDB(dbName).getCollection(collectionName).remove(documentData);

        } catch (MongoException e) {
            throw new DocumentException(ErrorCodes.DOCUMENT_DELETION_EXCEPTION, e.getMessage());
        }
        result = "Document with Data : [" + documentData + "] has been deleted.";
        return result;
    }
}
