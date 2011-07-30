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

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException; 
import com.imaginea.mongodb.common.exceptions.ValidationException;

/**
 * Defines services for performing operations like create/drop on collections
 * inside a database present in mongo to which we are connected to. Also
 * provides service to get list of all collections present and Statistics of a
 * particular collection.
 * 
 * @author Rachit Mittal
 * @since 4 July 2011
 * 
 * 
 */
public interface CollectionService {

	/**
	 * Gets the list of collections present in a database in mongo to which user
	 * is connected to.
	 * 
	 * @param dbName
	 *            Name of database
	 * @return List of All Collections present in MongoDb
	 * 
	 * @exception UndefinedDatabaseException
	 *                If db is not present
	 * @exception EmptyDatabaseNameException
	 *                If Db Name is null
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of EmptyDatabaseNameException
	 * @exception CollectionException
	 *                exception while performing get list operation on
	 *                collection
	 * 
	 */

	public Set<String> getCollList(String dbName) throws ValidationException, DatabaseException, CollectionException;

	/**
	 * Creates a collection inside a database in mongo to which user is
	 * connected to.
	 * 
	 * @param dbName
	 *            Name of Database in which to insert a collection
	 * @param collectionName
	 *            Name of Collection to be inserted
	 * @param capped
	 *            Specify if the collection is capped
	 * @param size
	 *            Specify the size of collection
	 * @param maxDocs
	 *            specify maximum no of documents in the collection
	 * @return Success if Insertion is successful else throw exception
	 * @exception EmptyDatabaseNameException
	 *                if dbName is null
	 * @exception EmptyCollectionNameException
	 *                if collectionName is null
	 * @exception UndefinedDatabaseException
	 *                if database is not present
	 * @exception DuplicateCollectionException
	 *                if collection is already present
	 * @exception InsertCollectionException
	 *                exception while inserting collection
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 * @exception CollectionException
	 *                throw super type of
	 *                DuplicateCollectionException,InsertCollectionException
	 */
	public String insertCollection(String dbName, String collectionName, boolean capped, int size, int maxDocs) throws DatabaseException,
			CollectionException, ValidationException;

	/**
	 * Deletes a collection inside a database in mongo to which user is
	 * connected to.
	 * 
	 * @param dbName
	 *            Name of Database in which to insert a collection
	 * @param collectionName
	 *            Name of Collection to be inserted
	 * @return Success if deletion is successful else throw exception
	 * @exception EmptyDatabaseNameException
	 *                if dbName is null
	 * @exception EmptyCollectionNameException
	 *                if collectionName is null
	 * @exception UndefinedDatabaseException
	 *                if database is not present
	 * @exception UndefinedCollectionException
	 *                if collection is not present
	 * @exception DeleteCollectionException
	 *                exception while deleting collection
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 * @exception CollectionException
	 *                throw super type of
	 *                UndefinedCollectionException,DeleteCollectionException
	 */

	public String deleteCollection(String dbName, String collectionName) throws DatabaseException, CollectionException, ValidationException;

	/**
	 * Get Statistics of a collection inside a database in mongo to which user
	 * is connected to.
	 * 
	 * @param dbName
	 *            Name of Database in which to insert a collection
	 * @param collectionName
	 *            Name of Collection to be inserted
	 * @return Array of JSON Objects each containing a key value pair in
	 *         Collection Stats.
	 * @exception EmptyDatabaseNameException
	 *                if dbName is null
	 * @exception EmptyCollectionNameException
	 *                if collectionName is null
	 * @exception UndefinedDatabaseException
	 *                if database is not present
	 * @exception UndefinedCollectionException
	 *                if collection is not present
	 * @exception DatabaseException
	 *                throw super type of UndefinedDatabaseException
	 * @exception ValidationException
	 *                throw super type of
	 *                EmptyDatabaseNameException,EmptyCollectionNameException
	 * @exception CollectionException
	 *                throw super type of UndefinedCollectionException
	 * @exception JSONException
	 *                JSON Exception
	 */

	public JSONArray getCollStats(String dbName, String collectionName) throws DatabaseException, CollectionException, ValidationException,
			JSONException;
}