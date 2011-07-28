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

import org.bson.types.ObjectId;  

import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.mongodb.DBObject;

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

	public ArrayList<DBObject> getQueriedDocsList(String dbName, String collectionName, DBObject query, DBObject keys, int limit, int skip)
			throws DatabaseException, CollectionException, DocumentException, ValidationException;

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

	public String insertDocument(String dbName, String collectionName, DBObject document) throws DatabaseException, CollectionException,
			DocumentException, ValidationException;

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

	public String updateDocument(String dbName, String collectionName, ObjectId id, DBObject newData) throws DatabaseException, CollectionException,
			DocumentException, ValidationException;

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

	public String deleteDocument(String dbName, String collectionName, ObjectId id) throws DatabaseException, CollectionException, DocumentException,
			ValidationException;

}
