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
