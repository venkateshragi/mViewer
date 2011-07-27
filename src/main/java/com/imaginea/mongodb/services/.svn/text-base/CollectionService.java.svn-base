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
import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
import com.imaginea.mongodb.common.exceptions.DuplicateCollectionException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.InsertCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;

/**
 * Defines an interface for different operations like get,create,delete defined
 * on collections present in Mongo Database.
 *
 */
public interface CollectionService {

	/**
	 * Get List of All Collections present in a <dbName>
	 *
	 * @param dbName
	 *            : Name of the Database whose collections list is to be
	 *            returned
	 * @return : Set of all Collections in <dbName>
	 * @throws EmptyDatabaseNameException
	 *             ,UndefinedDatabaseException,CollectionException
	 */

	public Set<String> getCollections(String dbName)
			throws EmptyDatabaseNameException, UndefinedDatabaseException,
			CollectionException;

	/**
	 * Creates a Collection <collectionName> inside db <dbName>
	 *
	 * @param dbName
	 *            : Name of Database in which to insert a collection
	 * @param collectionName
	 *            : Name of Collection to be inserted
	 * @param capped: Specify if the collection is capped
	 * @param size : Specify the size of collection
	 * @param maxDocs : specify maximum no of documents in the collection
	 * @return : Success if Insertion is successful else throw exception
	 * @throws EmptyDatabaseNameException
	 *             , EmptyCollectionNameException, UndefinedDatabaseException,
	 *             DuplicateCollectionException
	 */
	public String insertCollection(String dbName, String collectionName,
			boolean capped, int size, int maxDocs)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, DuplicateCollectionException,
			InsertCollectionException;

	/**
	 * Deletes a Collection <collectionName> inside db <dbName>
	 *
	 * @param dbName
	 *            : Name of Database from which to delete a collection
	 * @param collectionName
	 *            : Name of Collection to be deleted
	 * @return : Success if deletion is successful else throw exception
	 * @throws EmptyDatabaseNameException
	 *             , EmptyCollectionNameException, UndefinedDatabaseException,
	 *             UndefinedCollectionException, DeleteCollectionException
	 */
	public String deleteCollection(String dbName, String collectionName)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, UndefinedCollectionException,
			DeleteCollectionException;

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
	 *             , EmptyCollectionNameException, UndefinedDatabaseException,
	 *             UndefinedCollectionException, JSONParseException
	 */
	public JSONArray getCollectionStats(String dbName, String collectionName)
			throws EmptyDatabaseNameException, EmptyCollectionNameException,
			UndefinedDatabaseException, UndefinedCollectionException,
			JSONException;

}