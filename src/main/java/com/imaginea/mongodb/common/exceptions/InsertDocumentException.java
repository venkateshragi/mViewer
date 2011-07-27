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
package com.imaginea.mongodb.common.exceptions;

/**
 * Defines Exception generated while performing insert operation on a document.
 * This class extends DocumentException which is the General Exception for all
 * operations on Documents.
 *
 * @author Rachit Mittal
 *
 */
public class InsertDocumentException extends DocumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DeleteDocumentException with errorcode and message. in it.
	 * 
	 * 
	 * @param message
	 *            : A description about the Error.
	 * 
	 */
	public InsertDocumentException(String message) {
		super(ErrorCodes.DOCUMENT_CREATION_EXCEPTION, message);

	}

	/**
	 * Creates a new InsertDocumentException with errorcode and message and
	 * appending the cause of a caught exception.
	 * 
	 * 
	 * @param message
	 *            : A description about the Error.
	 * 
	 * @param cause
	 *            : Cause of the previous Exception. This is appended in the new
	 *            InsertDocumentException formed here.
	 */
	public InsertDocumentException(String message,
			Throwable cause) {
		super(ErrorCodes.DOCUMENT_CREATION_EXCEPTION, message, cause);

	}

}
