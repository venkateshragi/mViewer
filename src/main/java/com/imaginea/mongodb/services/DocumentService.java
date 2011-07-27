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

import com.imaginea.mongodb.common.exceptions.DeleteDocumentException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDocumentDataException;
import com.imaginea.mongodb.common.exceptions.InsertDocumentException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.exceptions.UpdateDocumentException;
import com.mongodb.DBObject;

/**
 * Defines the Interface for all the operations defined on documents inside
 * collections in databases in Mongo.
 *
 * @author Rachit Mittal
 *
 */
public interface DocumentService {
	/**
	 * GET List of Documents present in a <collectionName> inside a <dbName>
	 * after performing <query> and containing only <keys> keys.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection from which to get all Documents
	 *
	 * @param query
	 *            : query to be performed. In case of empty query {} return all
	 *            docs.
	 *
	 * @param keys
	 *            : Keys to be present in the resulted docs.
	 *
	 * @param limit
	 *            : Number of docs to show.
	 *
	 * @param skip
	 *            : Docs to skip from the front.
	 *
	 * @return : List of all documents in <dbName> and <collectionName>
	 * @throws EmptyDatabaseNameException
	 *             , EmptyCollectionNameException,DocumentException
	 */
	public ArrayList<DBObject> getDocuments(String dbName,
			String collectionName, DBObject query, DBObject keys, int limit,
			int skip) throws EmptyDatabaseNameException,
			EmptyCollectionNameException, DocumentException;

	/**
	 * Insert <documentData> in a <collectionName> inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection in which to insert a document
	 * @param document
	 *            : Document data to be inserted
	 * @return : Insertion Status
	 * @throws EmptyDatabaseNameException
	 *             EmptyCollectionNameException EmptyDocumentDataException
	 *             ,UndefinedDatabaseException
	 *             ,UndefinedCollectionException,InsertDocumentException
	 */
	public String insertDocument(String dbName, String collectionName,
			DBObject document) throws EmptyDatabaseNameException,
			EmptyCollectionNameException, EmptyDocumentDataException,
			UndefinedDatabaseException, UndefinedCollectionException,
			InsertDocumentException;

	/**
	 * Updates a document with Id <id> with <newData> in a <collectionName>
	 * inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection in which to update a document
	 * @param id
	 *            : Id of Document to be deleted
	 * @param newData
	 *            : Object with _id of the document to be updated and the keys
	 *            along with new values.
	 * @return : Update status
	 * @throws EmptyDatabaseNameException
	 *             , UndefinedDatabaseException, EmptyCollectionNameException,
	 *             UndefinedCollectionException, UndefinedDocumentException,
	 *             EmptyDocumentDataException, UpdateDocumentException
	 */
	public String updateDocument(String dbName, String collectionName,
			ObjectId id, DBObject newData) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, EmptyCollectionNameException,
			UndefinedCollectionException, UndefinedDocumentException,
			EmptyDocumentDataException, UpdateDocumentException,
			DocumentException;

	/**
	 * Deletes a document with Id <id> in a <collectionName> inside a <dbName>
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection from which to delete a document
	 * @param id
	 *            : Delete Document with this Id.
	 * @return : Deletion Status
	 * @throws EmptyDatabaseNameException
	 *             , UndefinedDatabaseException, EmptyCollectionNameException,
	 *             UndefinedCollectionException, UndefinedDocumentException,
	 *             EmptyDocumentDataException, DeleteDocumentException
	 */
	public String deleteDocument(String dbName, String collectionName,
			ObjectId id) throws EmptyDatabaseNameException,
			UndefinedDatabaseException, EmptyCollectionNameException,
			UndefinedCollectionException, UndefinedDocumentException,
			EmptyDocumentDataException, DeleteDocumentException;

}
