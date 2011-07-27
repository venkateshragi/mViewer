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
import java.util.Iterator;
import java.util.Set;

import org.bson.types.ObjectId;

import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class DocumentServiceImpl implements DocumentService {
	/**
	 * MongoInstanceProvider Instance
	 */
	private MongoInstanceProvider mongoInstanceProvider;
	/**
	 * Mongo Instance
	 */
	private Mongo mongoInstance;

	/**
	 * Creates an instance of MongoInstanceProvider based on userMappingKey
	 * recieved from Document Request Dispatcher.
	 *
	 * @param userMappingKey
	 *            : A combination of username , mongo Host and mongoPort
	 */
	public DocumentServiceImpl(String userMappingKey) {

		// TODO Beans
		mongoInstanceProvider = new SessionMongoInstanceProvider(userMappingKey);
	}

	/**
	 * GET List of Documents present in a <collectionName> inside a <dbName>
	 * after performing <query> and containing only <keys> keys.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection from which to get all Documents
	 *
	 * @param query
	 *            : query to be performed. In case of empty query {} return all
	 *            docs.
	 *
	 * @param keys
	 *            : Keys to be present in the resulted docs.
	 *
	 * @param limit
	 *            : Number of docs to show.
	 *
	 * @param skip
	 *            : Docs to skip from the front.
	 *
	 * @return : List of all documents in <dbName> and <collectionName>
	 * @throws EmptyDatabaseNameException
	 *             , EmptyCollectionNameException,DocumentException
	 */
	@Override
	public ArrayList<DBObject> getDocuments(String dbName,
			String collectionName, DBObject query, DBObject keys, int limit,
			int skip) throws EmptyDatabaseNameException,
			EmptyCollectionNameException, DocumentException {

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
			if (keys.keySet().isEmpty()) {
				keys.put("_id", 1); // For empty keys return all _id of all
									// Docs.
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
	 * Insert <documentData> in a <collectionName> inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection in which to insert a document
	 * @param document
	 *            : Document data to be inserted
	 * @return : Insertion Status
	 * @throws EmptyDatabaseNameException
	 *             EmptyCollectionNameException EmptyDocumentDataException
	 *             ,UndefinedDatabaseException,UndefinedCollectionException
	 */

	@Override
	public String insertDocument(String dbName, String collectionName,
			DBObject document) throws EmptyDatabaseNameException,
			EmptyCollectionNameException, EmptyDocumentDataException,
			UndefinedDatabaseException, UndefinedCollectionException,
			InsertDocumentException {
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
	 *
	 * Updates a document with Id <id> with <newData> in a <collectionName>
	 * inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection in which to update a document
	 * @param id
	 *            : Id of Document to be deleted
	 *
	 * @param newData
	 *            : Object with _id of the document to be updated and the keys
	 *            along with new values.
	 * @return : Update status
	 * @throws EmptyDatabaseNameException
	 *             , UndefinedDatabaseException, EmptyCollectionNameException,
	 *             UndefinedCollectionException,
	 *             DocumentException,UpdateDocumentException
	 */
	@Override
	public String updateDocument(String dbName, String collectionName,
			ObjectId id, DBObject newData) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, EmptyCollectionNameException,
			UndefinedCollectionException, UndefinedDocumentException,
			EmptyDocumentDataException, UpdateDocumentException,
			DocumentException {

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
			if (cursor.hasNext()) {
				documentData = cursor.next();
			}
		} catch (MongoException e) {
			throw new UpdateDocumentException(

			"DOCUMENT_UPDATE_EXCEPTION");
		}
		result = "Updated Document: [" + documentData + "]";

		return result;
	}

	/**
	 * Deletes a document with Id <id> in a <collectionName> inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection from which to delete a document
	 * @param id
	 *            : Delete Document with this Id.
	 * @return : Deletion Status
	 */

	@Override
	public String deleteDocument(String dbName, String collectionName,
			ObjectId id) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, EmptyCollectionNameException,
			UndefinedCollectionException, UndefinedDocumentException,
			EmptyDocumentDataException, DeleteDocumentException {
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
				documentData = cursor.next(); // _id is primary key so one time
												// loop
												// execution only
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
