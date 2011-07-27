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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
import com.imaginea.mongodb.common.exceptions.DuplicateCollectionException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Deinfes All the Operations available on Collections in MongoDb
 *
 * @author Rachit Mittal
 */

public class CollectionServiceImpl implements CollectionService {

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
	 * recieved from Collection Request Dispatcher.
	 *
	 * @param userMappingKey
	 *            : A combination of username , mongo Host and mongoPort
	 */
	public CollectionServiceImpl(String userMappingKey) {

		// TODO Beans
		mongoInstanceProvider = new SessionMongoInstanceProvider(userMappingKey);
	}

	/**
	 * Get List of All Collections present in a <dbName>
	 *
	 * @param dbName
	 *            : Name of the Database whose collections list is to be
	 *            returned
	 * @return : List of all Collections in <dbName>
	 * @throws EmptyDatabaseNameException
	 *             ,UndefinedDatabaseException,CollectionException
	 */
	@Override
	public Set<String> getCollections(String dbName)
			throws EmptyDatabaseNameException, UndefinedDatabaseException,
			CollectionException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();

		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database Name Is Null");
		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		Set<String> collList = new HashSet<String>();

		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException(

				"Database with dbName [ " + dbName + "] does not exist");
			}

			Set<String> collectionList = new HashSet<String>();
			collectionList = mongoInstance.getDB(dbName).getCollectionNames();
			Iterator<String> it = collectionList.iterator();

			while (it.hasNext()) {
				String coll = it.next();
				if (!coll.contains("system.")) {
					collList.add(coll);
				}
			}

		} catch (MongoException m) {
			CollectionException e = new CollectionException(
					ErrorCodes.GET_COLLECTION_LIST_EXCEPTION,
					"GET_COLLECTION_LIST_EXCEPTION", m.getCause());
			throw e;
		}
		return collList;

	}

	/**
	 * Creates a Collection <collectionName> inside db <dbName>
	 *
	 * @param dbName
	 *            : Name of Database in which to insert a collection
	 * @param collectionName
	 *            : Name of Collection to be inserted
	 * @param capped
	 *            : Specify if the collection is capped
	 * @param size
	 *            : Specify the size of collection
	 * @param maxDocs
	 *            : specify maximum no of documents in the collection
	 * @return : Success if Insertion is successful else throw exception
	 * @throws EmptyDatabaseNameException
	 *             , EmptyCollectionNameException, UndefinedDatabaseException,
	 *             DuplicateCollectionException
	 */
	@Override
	public String insertCollection(String dbName, String collectionName,
			boolean capped, int size, int maxDocs)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, DuplicateCollectionException,
			InsertCollectionException {

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
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("Db with name [" + dbName
						+ "] doesn't exist.");
			}
			if (mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new DuplicateCollectionException(
						ErrorCodes.COLLECTION_ALREADY_EXISTS, "Collection ["
								+ collectionName
								+ "] Already exists in Database [" + dbName
								+ "]");
			}

			DBObject options = new BasicDBObject();
			options.put("capped", capped);
			if (capped) {
				options.put("size", size);
				options.put("max", maxDocs);
			}
			mongoInstance.getDB(dbName).createCollection(collectionName,
					options);
		} catch (MongoException m) {
			InsertCollectionException e = new InsertCollectionException(
					"COLLECTION_CREATION_EXCEPTION", m.getCause());
			throw e;
		}
		String result = "Created Collection [" + collectionName
				+ "] in Database [" + dbName + "]";

		return result;
	}

	/**
	 * Deletes a Collection <collectionName> inside db <dbName>
	 *
	 * @param dbName
	 *            : Name of Database from which to delete a collection
	 * @param collectionName
	 *            : Name of Collection to be deleted
	 * @return : Success if deletion is successful else throw exception
	 * @throws EmptyDatabaseNameException
	 *             ,EmptyCollectionNameException,UndefinedDatabaseException,
	 *             UndefinedCollectionException,DeleteCollectionException
	 */
	@Override
	public String deleteCollection(String dbName, String collectionName)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, UndefinedCollectionException,
			DeleteCollectionException {

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
		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB with name [" + dbName
						+ "]DOES_NOT_EXIST");
			}

			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException(

				"Collection with name [" + collectionName
						+ "] DOES NOT EXIST in Database [" + dbName + "]");
			}

			mongoInstance.getDB(dbName).getCollection(collectionName).drop();
		} catch (MongoException m) {

			DeleteCollectionException e = new DeleteCollectionException(
					"COLLECTION_DELETION_EXCEPTION", m.getCause());
			throw e;

		}
		String result = "Deleted Collection [" + collectionName
				+ "] in Database [" + dbName + "]";

		return result;
	}

	/**
	 * Return Stats of a particular Collection <collectionName> in a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection
	 * @return : Array of JSON Objects each containing a key value pair in
	 *         Collection Stats.
	 * @throws EmptyDatabaseNameException
	 *             ,EmptyCollectionNameException,UndefinedDatabaseException,
	 *             UndefinedCollectionException
	 */
	@Override
	public JSONArray getCollectionStats(String dbName, String collectionName)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, UndefinedCollectionException,
			CollectionException, JSONException {
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

		JSONArray collStats = new JSONArray();

		try {
			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
				throw new UndefinedDatabaseException("DB with name [" + dbName
						+ "]DOES_NOT_EXIST");
			}
			if (!mongoInstance.getDB(dbName).getCollectionNames()
					.contains(collectionName)) {
				throw new UndefinedCollectionException(

				"Collection with name [" + collectionName
						+ "] DOES NOT EXIST in Database [" + dbName + "]");
			}
			CommandResult stats = mongoInstance.getDB(dbName)
					.getCollection(collectionName).getStats();

			Set<String> keys = stats.keySet();
			Iterator<String> keyIterator = keys.iterator();
			JSONObject temp = new JSONObject();

			while (keyIterator.hasNext()) {
				temp = new JSONObject();
				String key = keyIterator.next().toString();
				temp.put("Key", key);
				String value = stats.get(key).toString();
				temp.put("Value", value);
				String type = stats.get(key).getClass().toString();
				temp.put("Type", type.substring(type.lastIndexOf('.') + 1));
				collStats.put(temp);
			}
		} catch (JSONException e) {
			throw e;
		} catch (MongoException m) {
			CollectionException e = new CollectionException(
					ErrorCodes.GET_COLL_STATS_EXCEPTION,
					"GET_COLL_STATS_EXCEPTION", m.getCause());
			throw e;
		}
		return collStats;
	}
}
