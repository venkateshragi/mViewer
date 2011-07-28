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