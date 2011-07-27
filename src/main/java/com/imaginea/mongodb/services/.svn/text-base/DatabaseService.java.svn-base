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
import com.imaginea.mongodb.common.exceptions.DeleteDatabaseException;
import com.imaginea.mongodb.common.exceptions.DuplicateDatabaseException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.InsertDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.ValidationException;

/**
 * Defines operations to create/update/drop/query databases of mongo instance
 * we are currently connected to. Also provides the statistics for the database.
 *
 * @author Rachit Mittal
 *
 */
public interface DatabaseService {
	/**
	 * Get All Databases present in Mongo Db.
	 *
	 * @return List of Database Names.
	 * @throws DatabaseException
	 */
	public List<String> getAllDb() throws DatabaseException;//TODO method names to follow convention

	/**
	 * Return Stats of a particular Database
	 *
	 * @param dbName
	 *            : Name of Database
	 * @return : Array of JSON Objects each containing a key value pair in Db
	 *         Stats.
	 * @throws DatabaseException
	 *             : If creation of MongoInstance is failed.
	 * @throws ValidationException
	 *             : If dbName is null.
	 */
	public JSONArray getDbStats(String dbName)
			throws EmptyDatabaseNameException, UndefinedDatabaseException,
			DatabaseException, JSONException;

	/**
	 * Create a Databse with a name <dbName>
	 *
	 * @param dbName
	 *            : Name of Database to be created
	 * @return : Success if Created else throws Exception
	 */

	public String createDb(String dbName) throws EmptyDatabaseNameException,
			DuplicateDatabaseException, InsertDatabaseException;

	/**
	 * Delete a Databse with a name <dbName>
	 *
	 * @param dbName
	 *            : Name of Database to be created
	 * @return : Success if Deleted else throws Exception
	 */
	public String dropDb(String dbName) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, DeleteDatabaseException;

}
