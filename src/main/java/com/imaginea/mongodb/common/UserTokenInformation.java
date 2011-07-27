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
package com.imaginea.mongodb.common;


/**
 * A Token has been defined which is randomly generated for every user.
 * Corresponding to every token there is a combination of username , mongoHost
 * and mongoPort which describes the Mongo Instance on which that user works.
 *
 *
 *
 * @author Rachit
 *
 */
public interface UserTokenInformation {
	/**
	 * Generates a random 24 byte <tokenID> for every user. This <tokenID> is
	 * used by SessionMongoInstanceProvider to search for a unique MongoInstance
	 * for that User.
	 *
	 * @return tokenId.
	 */
	public String generateTokenId();

	/**
	 * Get Mongo Host for a token.
	 *
	 * @return Mongo Host Address
	 */
	public String getMongoHost();

	/**
	 * Get Mongo Port for a token.
	 *
	 * @return Mongo Port
	 */
	public int getMongoPort();
	/**
	 * Get UserName for a token.
	 *
	 * @return UserName
	 */
	public String getUsername();

	/**
	 * Get tokenID for a token.
	 *
	 * @return tokenId
	 */
	public String getTokenId();

}
