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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteDatabaseException;
import com.imaginea.mongodb.common.exceptions.DuplicateDatabaseException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Defines All the Operations available on Databases in MongoDb
 *
 * @author Rachit Mittal
 *
 */
public class DatabaseServiceImpl implements DatabaseService {

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
	 * recieved from Database Request Dispatcher.
	 *
	 * @param userMappingKey
	 *            : A combination of username , mongo Host and mongoPort
	 */
	public DatabaseServiceImpl(String userMappingKey) {

		// TODO Beans
		mongoInstanceProvider = new SessionMongoInstanceProvider(userMappingKey);
	}

	/**
	 * Service function to get list of all databases.
	 *
	 * @return List of All Databases present in MongoDb
	 *
	 * @throws DatabaseException
	 *             : If Cannot get DB List.
	 */

	@Override
	public List<String> getAllDb() throws DatabaseException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();
		List<String> dbNames;
		try {
			dbNames = mongoInstance.getDatabaseNames();
		} catch (MongoException m) {
			DatabaseException e = new DatabaseException(
					ErrorCodes.GET_DB_LIST_EXCEPTION, "GET_DB_LIST_EXCEPTION",
					m.getCause());

			throw e;
		}
		return dbNames;

	}

	/**
	 * Creates a Database with a name <dbName>
	 *
	 * @param dbName
	 *            : Name of Database to be created
	 * @return : Success if Created else throws Exception
	 * @throws EmptyDatabaseNameException
	 *             : When dbName is null
	 * @throws DuplicateDatabaseException
	 *             : when db Already present
	 * @throws InsertDatabaseException
	 *             : When tries to create Db and getting collections list
	 *
	 */

	@Override
	public String createDb(String dbName) throws EmptyDatabaseNameException,
			DuplicateDatabaseException, InsertDatabaseException {
		mongoInstance = mongoInstanceProvider.getMongoInstance();

		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		try {
			boolean dbAlreadyPresent = mongoInstance.getDatabaseNames()
					.contains(dbName);
			if (dbAlreadyPresent) {
				throw new DuplicateDatabaseException("DB with name [" + dbName
						+ "] ALREADY EXISTS");
			}

			mongoInstance.getDB(dbName).getCollectionNames();
		} catch (MongoException e) {

			throw new InsertDatabaseException("DB_CREATION_EXCEPTION",
					e.getCause());
		}

		String result = "Created DB with name [" + dbName + "]";
		return result;
	}

	/**
	 * Delete a Databse with a name <dbName>
	 *
	 * @param dbName
	 *            : Name of Database to be created
	 * @return : Success if Deleted else throws Exception
	 *
	 * @throws EmptyDatabaseNameException
	 *             :When DbName Empty
	 * @throws UndefinedDatabaseException
	 *             : When Db not present and try to delete it
	 *
	 * @throws DeleteDatabaseException
	 *             : Error while performing this operation
	 */
	@Override
	public String dropDb(String dbName) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, DeleteDatabaseException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();
		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}
		try {
			boolean dbPresent = mongoInstance.getDatabaseNames().contains(
					dbName);
			if (!dbPresent) {
				throw new UndefinedDatabaseException("DB with name [" + dbName
						+ "]  DOES NOT EXIST");
			}

			mongoInstance.dropDatabase(dbName);
		} catch (MongoException e) {

			throw new DeleteDatabaseException("DB_DELETION_EXCEPTION",
					e.getCause());
		}

		String result = "Deleted DB with name [" + dbName + "]";
		return result;
	}

	/**
	 * Return Stats of a particular Database
	 *
	 * @param dbName
	 *            : Name of Database
	 * @return : Array of JSON Objects each containing a key value pair in Db
	 *         Stats.
	 * @throws EmptyDatabaseNameException
	 *             : DbName is empty
	 * @throws UndefinedDatabaseException
	 *             : Db not present
	 * @throws JSONParseException
	 *             : While parsing JSON
	 * @throws DatabaseException
	 *             : Error while performing this operation
	 */
	@Override
	public JSONArray getDbStats(String dbName)
			throws EmptyDatabaseNameException, UndefinedDatabaseException,
			DatabaseException, JSONException {

		mongoInstance = mongoInstanceProvider.getMongoInstance();
		if (dbName == null) {
			throw new EmptyDatabaseNameException("Database name is null");

		}
		if (dbName.equals("")) {
			throw new EmptyDatabaseNameException("Database Name Empty");
		}

		JSONArray dbStats = new JSONArray();
		try {
			boolean dbPresent = mongoInstance.getDatabaseNames().contains(
					dbName);
			if (!dbPresent) {
				throw new UndefinedDatabaseException("DB with name [" + dbName
						+ "]  DOES NOT EXIST");
			}

			DB db = mongoInstance.getDB(dbName);
			CommandResult stats = db.getStats();
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
				dbStats.put(temp);
			}
		} catch (JSONException e) {
			throw e;
		} catch (MongoException m) {
			throw new DatabaseException(ErrorCodes.GET_DB_STATS_EXCEPTION,
					"GET_DB_STATS_EXCEPTION");

		}

		return dbStats;
	}
}
