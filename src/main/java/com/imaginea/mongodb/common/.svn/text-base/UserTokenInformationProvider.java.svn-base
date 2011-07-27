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
 * Stroes the information about a token for a User and provides the
 * implementation of generating tokenID.
 * 
 * @author Rachit Mittal
 * 
 */
public class UserTokenInformationProvider implements UserTokenInformation {

	private String tokenID;
	private String mongoHost;
	private int mongoPort;
	private String userName;

	/**
	 * Stores Information about a User token.
	 *
	 * @param mongoHost
	 *            mongo Db IP Addresss.
	 * @param mongoPort
	 *            mongo Db Port
	 * @param userName
	 *            : Name of User.
	 */
	public UserTokenInformationProvider(String mongoHost, int mongoPort,
			String userName) {
		this.mongoHost = mongoHost;
		this.mongoPort = mongoPort;
		this.userName = userName;
	}

	/**
	 * Generates 24 byte token ID for a User.
	 *
	 * @return tokenID
	 */
	public String generateTokenId()

	{
		String id = "";
		for (int position = 0; position < 24; position++) {
			int temp = (int) (Math.random() * 10);
			id += temp;
		}

		tokenID = id.toString();
		return tokenID;
	}

	/**
	 * Get Mongo Host for a token.
	 *
	 * @return Mongo Host Address
	 */
	public String getMongoHost() {
		return mongoHost;
	}

	/**
	 * Get Mongo Port for a token.
	 *
	 * @return Mongo Port
	 */
	public int getMongoPort() {
		return mongoPort;
	}

	/**
	 * Get UserName for a token.
	 *
	 * @return UserName
	 */
	public String getUsername() {
		return userName;
	}

	/**
	 * Get tokenID for a token.
	 *
	 * @return tokenId
	 */
	public String getTokenId() {
		return tokenID;
	}

}
