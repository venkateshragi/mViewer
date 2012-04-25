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

import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.mongodb.DBObject;

import java.util.ArrayList;

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
public interface DocumentService {
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

	public ArrayList<DBObject> getQueriedDocsList(String dbName, String collectionName, DBObject query, DBObject keys, int limit, int skip) throws DatabaseException, CollectionException,
			DocumentException, ValidationException;

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

	public String insertDocument(String dbName, String collectionName, DBObject document) throws DatabaseException, CollectionException, DocumentException, ValidationException;

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
	 * 
	 */

	public String updateDocument(String dbName, String collectionName, String _id, DBObject newData) throws DatabaseException, CollectionException, DocumentException, ValidationException;

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

	public String deleteDocument(String dbName, String collectionName, String _id) throws DatabaseException, CollectionException, DocumentException, ValidationException;

}
