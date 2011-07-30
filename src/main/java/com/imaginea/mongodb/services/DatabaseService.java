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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.imaginea.mongodb.common.exceptions.DatabaseException;  
import com.imaginea.mongodb.common.exceptions.ValidationException;

/**
 * Defines services for performing operations like create/drop on databases
 * present in mongo to whic we are connected to. Also provides service to get
 * list of all databases present and Statistics of a particular database.
 * 
 * @author Rachit Mittal
 * @since 2 July 2011
 * 
 * 
 */
public interface DatabaseService {

	/**
	 * Gets the list of databases present in mongo to which user is
	 * connected to.
	 * 
	 * @return List of All Databases present in MongoDb
	 * 
	 * @throws DatabaseException
	 *             If any error while getting database list.
	 */

	public List<String> getDbList() throws DatabaseException; 
	/**
	 * Return Stats of a particular Database in mongo to which user is connected
	 * to.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @return Array of JSON Objects each containing a key value pair in Db
	 *         Stats.
	 * @exception EmptyDatabaseNameException
	 *                DbName is empty
	 * @exception UndefinedDatabaseException
	 *                Db not present
	 * @exception JSONException
	 *                While parsing JSON
	 * @exception DatabaseException
	 *                Error while performing this operation
	 * @exception ValidationException
	 *                throw super type of EmptyDatabaseNameException
	 */
	public JSONArray getDbStats(String dbName) throws DatabaseException, ValidationException, JSONException;

	/**
	 * Creates a Database with the specified name in mongo database to which
	 * user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database to be created
	 * @return Success if Created else throws Exception
	 * 
	 * @exception EmptyDatabaseNameException
	 *                When dbName is null
	 * @exception DuplicateDatabaseException
	 *                When database is already present
	 * @exception InsertDatabaseException
	 *                Any exception while trying to create db
	 * @exception DatabaseException
	 *                throw super type of
	 *                DuplicateDatabaseException,InsertDatabaseException
	 * @exception ValidationException
	 *                throw super type of EmptyDatabaseNameException
	 * 
	 * 
	 */

	public String createDb(String dbName) throws DatabaseException, ValidationException;

	/**
	 * Deletes a Database with the specified name in mongo database to which
	 * user is connected to.
	 * 
	 * @param dbName
	 *            Name of Database to be deleted
	 * @return Success if deleted else throws Exception
	 * 
	 * @exception EmptyDatabaseNameException
	 *                When dbName is null
	 * @exception UndefinedDatabaseException
	 *                When database is not present
	 * @exception DeleteDatabaseException
	 *                Any exception while trying to create db
	 * @exception DatabaseException
	 *                throw super type of
	 *                UndefinedDatabaseException,DeleteDatabaseException
	 * @exception ValidationException
	 *                throw super type of EmptyDatabaseNameException
	 * 
	 * 
	 */
	public String dropDb(String dbName) throws DatabaseException, ValidationException;

}
