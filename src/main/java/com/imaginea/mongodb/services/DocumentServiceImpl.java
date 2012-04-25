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

package com.imaginea.mongodb.services;

import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Defines services definitions for performing operations like
 * create/update/delete on documents inside a collection in a database present
 * in mongo to which we are connected to. Also provides service to get list of
 * all documents present.
 * 
 * @author Rachit Mittal
 * @since 6 July 2011
 * 
 * 
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
	 * @param dbInfo
	 *            A combination of username,mongoHost and mongoPort
	 */
	public DocumentServiceImpl(String dbInfo) {
		mongoInstanceProvider = new SessionMongoInstanceProvider(dbInfo);
	}

	/**
	 * Gets the list of documents inside a collection in a database in mongo to
	 * which user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection from which to get all Documents
	 * 
	 * @param query
	 *            query to be performed. In case of empty query {} return all
	 *            docs.
	 * 
	 * @param keys
	 *            Keys to be present in the resulted docs.
	 * 
	 * @param limit
	 *            Number of docs to show.
	 * 
	 * @param skip
	 *            Docs to skip from the front.
	 * 
	 * @return List of all documents.
	 * @exception EmptyDatabaseNameException
	 *                If database name is null
	 * @exception EmptyCollectionNameException
	 *                If Collection name is null
	 * @exception UndefinedDatabaseException
	 *                If database is not present
	 * @exception UndefinedCollectionException
	 *                If Collection is not present
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 * @exception CollectionException
	 *                throw super type of UndefinedCollectionException
	 * @exception DocumentException
	 *                exception while performing get doc list
	 * 
	 */

	public ArrayList<DBObject> getQueriedDocsList(String dbName, String collectionName, DBObject query, DBObject keys, int limit, int skip) throws DatabaseException, CollectionException,
			DocumentException, ValidationException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();

		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		if (collectionName == null) {
			throw new EmptyCollectionNameException("Collection name is null");
		}
		if (collectionName.equals("")) {
			throw new EmptyCollectionNameException("Collection Name Empty");
		}

		ArrayList<DBObject> dataList = new ArrayList<DBObject>();
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB with name [" + dbName + "]DOES_NOT_EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
				throw new UndefinedCollectionException("Collection with name [" + collectionName + "] DOES NOT EXIST in Database [" + dbName + "]");
			}
			if (keys.keySet().isEmpty()) {
				keys.put("_id", 1); // For empty keys return all _id of all docs
			}

			// Return Queried Documents
			DBCursor cursor = mongoInstance.getDB(dbName).getCollection(collectionName).find(query, keys);
			cursor.limit(limit);
			cursor.skip(skip);

			if (cursor.hasNext()) {
				while (cursor.hasNext()) {
					dataList.add(cursor.next());
				}
			}
		} catch (MongoException e) {
			throw new DocumentException(ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION, "GET_DOCUMENT_LIST_EXCEPTION", e.getCause());
		}
		return dataList;

	}

	/**
	 * Insert a document inside a collection in a database in mongo to which
	 * user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection from which to get all Documents
	 * 
	 * @param document
	 *            : Document data to be inserted
	 * @return : Insertion Status
	 * @exception EmptyDatabaseNameException
	 *                If database name is null
	 * @exception EmptyCollectionNameException
	 *                If Collection name is null
	 * @exception EmptyDocumentDataException
	 *                If Document data is null
	 * @exception UndefinedDatabaseException
	 *                If database is not present
	 * @exception UndefinedCollectionException
	 *                If Collection is not present
	 * @exception InsertDocumentException
	 *                Any exception while inserting document
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 *                ,EmptyDocumentDataException
	 * @exception CollectionException
	 *                throw super type of UndefinedCollectionException
	 * @exception DocumentException
	 *                throw super type of InsertDocumentException
	 * 
	 */

	public String insertDocument(String dbName, String collectionName, DBObject document) throws DatabaseException, CollectionException, DocumentException, ValidationException {
		mongoInstance = mongoInstanceProvider.getMongoInstance();
		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		if (collectionName == null) {
			throw new EmptyCollectionNameException("Collection name is null");
		}
		if (collectionName.equals("")) {
			throw new EmptyCollectionNameException("Collection Name Empty");
		}

		String result = null;
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
			}

			// MongoDb permits Duplicate document Insert
			mongoInstance.getDB(dbName).getCollection(collectionName).insert(document);
			result = "Inserted Document with Data : [" + document + "]";
		}  catch (MongoException e) {
			throw new InsertDocumentException("DOCUMENT_CREATION_EXCEPTION", e.getCause());
		}
		return result;
	}

	/**
	 * Updates a document inside a collection in a database in mongo to which
	 * user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection from which to get all Documents
	 * @param _id
	 *            Id of Document to be updated
	 * @param newData
	 *            new Document value.
	 * @return Update status
	 * @exception EmptyDatabaseNameException
	 *                If database name is null
	 * @exception EmptyCollectionNameException
	 *                If Collection name is null
	 * @exception EmptyDocumentDataException
	 *                If Document data is null
	 * @exception UndefinedDatabaseException
	 *                If database is not present
	 * @exception UndefinedCollectionException
	 *                If Collection is not present
	 * @exception UpdateDocumentException
	 *                Any exception while updating document
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 *                ,EmptyDocumentDataException
	 * @exception CollectionException
	 *                throw super type of UndefinedCollectionException
	 * @exception DocumentException
	 *                throw super type of UpdateDocumentException
	 * @exception JSONException
	 * 
	 */

	public String updateDocument(String dbName, String collectionName, String _id, DBObject newData) throws DatabaseException, CollectionException, DocumentException, ValidationException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();
		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		if (collectionName == null) {
			throw new EmptyCollectionNameException("Collection name is null");
		}
		if (collectionName.equals("")) {
			throw new EmptyCollectionNameException("Collection Name Empty");
		}
		String result = null;
		DBObject documentData = null;
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
			}
			if (_id == null) {
				throw new EmptyDocumentDataException("Document is empty");
			}

			Object newId = newData.get("_id");
			if (newId == null) {
				throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "INVALID_OBJECT_ID");
			}
			if (newId.equals("")) {
				throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "INVALID_OBJECT_ID");
			}

			Object docId = JSON.parse(_id);
			if (!newId.equals(docId)) {
				throw new DocumentException(ErrorCodes.UPDATE_OBJECT_ID_EXCEPTION, "UPDATE_OBJECT_ID_EXCEPTION");
			} else {
				// Id's equal but putting the id of old document still
				// as newData as id of string type but we need ObjectId type
				newData.put("_id", docId);
			}
			DBObject query = new BasicDBObject("_id", docId);

			DBCollection collection = mongoInstance.getDB(dbName).getCollection(collectionName);
			DBObject doc = collection.findOne(query);
			if (doc == null) {
				throw new UndefinedDocumentException("DOCUMENT_DOES_NOT_EXIST");
			}

			collection.update(doc, newData, true, false);
			documentData = collection.findOne(query);

		} catch (IllegalArgumentException e) {
			// When error converting object Id
			throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID, "INVALID_OBJECT_ID");

		} catch (MongoException e) {
			throw new UpdateDocumentException("DOCUMENT_UPDATE_EXCEPTION");
		}
		result = "Document: [" + documentData + "] has been updated.";

		return result;
	}

	/**
	 * Deletes a document inside a collection in a database in mongo to which
	 * user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection from which to get all Documents
	 * @param _id
	 *            Id of Document to be updated
	 * @return Deletion status
	 * @exception EmptyDatabaseNameException
	 *                If database name is null
	 * @exception EmptyCollectionNameException
	 *                If Collection name is null
	 * @exception EmptyDocumentDataException
	 *                If Document data is null
	 * @exception UndefinedDatabaseException
	 *                If database is not present
	 * @exception UndefinedCollectionException
	 *                If Collection is not present
	 * @exception DeleteDocumentException
	 *                Any exception while deleting document
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 *                ,EmptyDocumentDataException
	 * @exception CollectionException
	 *                throw super type of UndefinedCollectionException
	 * @exception DocumentException
	 *                throw super type of DeleteDocumentException
	 * 
	 */

	public String deleteDocument(String dbName, String collectionName, String _id) throws DatabaseException, CollectionException, DocumentException, ValidationException {
		mongoInstance = mongoInstanceProvider.getMongoInstance();
		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		if (collectionName == null) {
			throw new EmptyCollectionNameException("Collection name is null");
		}
		if (collectionName.equals("")) {
			throw new EmptyCollectionNameException("Collection Name Empty");
		}

		String result = null;
		DBObject documentData = null;
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ " + collectionName + "] _DOES_NOT_EXIST in Db [ " + dbName + "]");
			}
			DBCollection coll = mongoInstance.getDB(dbName).getCollection(collectionName);
			if (coll.isCapped()) {
				throw new DocumentException(ErrorCodes.DELETING_FROM_CAPPED_COLLECTION, "Cannot Delete From a Capped Collection");
			}
			if (_id == null) {
				throw new EmptyDocumentDataException("Document is empty");
			}

			DBObject query = new BasicDBObject();
            Object docId = JSON.parse(_id);
			query.put("_id", docId);
			DBCollection collection = this.mongoInstance.getDB(dbName).getCollection(collectionName);
			documentData = collection.findOne(query);

			if (documentData == null) {
				throw new UndefinedDocumentException("DOCUMENT_DOES_NOT_EXIST");
			}

			mongoInstance.getDB(dbName).getCollection(collectionName).remove(documentData);

		} catch (MongoException e) {
			throw new DeleteDocumentException("DOCUMENT_DELETION_EXCEPTION");
		}
		result = "Document with Data : [" + documentData + "] has been deleted.";
		return result;
	}
}
