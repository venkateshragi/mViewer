/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.imaginea.mongodb.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bson.types.ObjectId;
import org.json.JSONException;

import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteDocumentException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDocumentDataException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDocumentException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.exceptions.UpdateDocumentException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

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

// TODO check update
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
	 * @param userMappingKey
	 *            A combination of username,mongoHost and mongoPort
	 */
	public DocumentServiceImpl(String userMappingKey) {
		mongoInstanceProvider = new SessionMongoInstanceProvider(userMappingKey);
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

	public ArrayList<DBObject> getQueriedDocsList(String dbName,
			String collectionName, DBObject query, DBObject keys, int limit,
			int skip) throws DatabaseException, CollectionException,
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
				throw new UndefinedDatabaseException("DB with name [" + dbName
						+ "]DOES_NOT_EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException("Collection with name ["
						+ collectionName + "] DOES NOT EXIST in Database ["
						+ dbName + "]");
			}
			if (keys.keySet().isEmpty()) {
				keys.put("_id", 1); // For empty keys return all _id of all docs
			}

			// Return Queried Documents
			DBCursor cursor = mongoInstance.getDB(dbName)
					.getCollection(collectionName).find(query, keys);
			cursor.limit(limit);
			cursor.skip(skip);

			if (cursor.hasNext()) {
				while (cursor.hasNext()) {
					dataList.add(cursor.next());
				}
			}
		} catch (MongoException e) {
			throw new DocumentException(ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION,
					"GET_DOCUMENT_LIST_EXCEPTION", e.getCause());
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

	public String insertDocument(String dbName, String collectionName,
			DBObject document) throws DatabaseException, CollectionException,
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

		String result = null;
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB [" + dbName
						+ "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ "
						+ collectionName + "] _DOES_NOT_EXIST in Db [ "
						+ dbName + "]");
			}

			// MongoDb permits Duplicate document Insert

			mongoInstance.getDB(dbName).getCollection(collectionName)
					.insert(document);
			result = "Inserted Document with Data : [" + document + "]";
		} catch (MongoException e) {
			throw new InsertDocumentException("DOCUMENT_CREATION_EXCEPTION",
					e.getCause());
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
	 * @param id
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

	public String updateDocument(String dbName, String collectionName,
			ObjectId id, DBObject newData) throws DatabaseException,
			CollectionException, DocumentException, ValidationException {

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
				throw new UndefinedDatabaseException("DB [" + dbName
						+ "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ "
						+ collectionName + "] _DOES_NOT_EXIST in Db [ "
						+ dbName + "]");
			}
			if (id == null) {
				throw new EmptyDocumentDataException("Document is empty");
			}
			String temp = (String) newData.get("_id");
			if (temp == null) {
				throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID,
						"INVALID_OBJECT_ID");
			}
			ObjectId newId = new ObjectId(temp);
			if (newId.equals("")) {
				throw new DocumentException(ErrorCodes.INVALID_OBJECT_ID,
						"INVALID_OBJECT_ID");
			}

			DBObject query = new BasicDBObject("_id", id);
			DBCollection collection = mongoInstance.getDB(dbName)
					.getCollection(collectionName);
			DBCursor cursor = collection.find(query);

			if (cursor.hasNext()) {
				documentData = cursor.next(); // _id is primary key so "if" used

				// execution only
			} else {
				throw new UndefinedDocumentException("DOCUMENT_DOES_NOT_EXIST"
						+ cursor.hasNext());
			}

			// Extract data from Request Body
			Set<String> keySet = newData.keySet();
			Iterator<String> keyIterator = keySet.iterator();

			BasicDBObject set = new BasicDBObject();
			DBObject newValues = new BasicDBObject();

			// Updates and add new fields
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				newValues = new BasicDBObject(key, newData.get(key));
				set.put("$set", newValues);
				mongoInstance.getDB(dbName).getCollection(collectionName)
						.update(query, set);
			}

			// Get New Document
			query = new BasicDBObject("_id", newId);
			collection = mongoInstance.getDB(dbName).getCollection(
					collectionName);
			cursor = collection.find(query);

			DBObject doc = new BasicDBObject();
			if (cursor.hasNext()) {
				documentData = cursor.next();
				doc = documentData;
			}

			// Remove Old Fields
			keySet = documentData.keySet();
			Set<String> deleteKeys = new HashSet<String>();
			keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();

				if (newData.get(key) == null) {
					deleteKeys.add(key);
				}
			}
			for(String key : deleteKeys)
			{
				documentData.removeField(key);
			} 
			collection.remove(doc);
			collection.insert(documentData);

		} catch (MongoException e) {
			throw new UpdateDocumentException("DOCUMENT_UPDATE_EXCEPTION");
		}
		result = "Updated Document: [" + documentData + "]";

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
	 * @param id
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

	public String deleteDocument(String dbName, String collectionName,
			ObjectId id) throws DatabaseException, CollectionException,
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

		String result = null;
		DBObject documentData = null;
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB [" + dbName
						+ "] DOES NOT EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException("COLLECTION [ "
						+ collectionName + "] _DOES_NOT_EXIST in Db [ "
						+ dbName + "]");
			}
			if (id == null) {
				throw new EmptyDocumentDataException("Document is empty");
			}

			DBObject query = new BasicDBObject();
			query.put("_id", id);
			DBCollection collection = this.mongoInstance.getDB(dbName)
					.getCollection(collectionName);
			DBCursor cursor = collection.find(query);

			if (cursor.hasNext()) {
				documentData = cursor.next();
			} else {
				throw new UndefinedDocumentException("DOCUMENT_DOES_NOT_EXIST");
			}

			mongoInstance.getDB(dbName).getCollection(collectionName)
					.remove(documentData);

		} catch (MongoException e) {
			throw new DeleteDocumentException("DOCUMENT_DELETION_EXCEPTION");
		}
		result = "Deleted Document with Data : [" + documentData + "]";
		return result;
	}
}
