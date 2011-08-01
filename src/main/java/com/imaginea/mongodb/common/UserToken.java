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
package com.imaginea.mongodb.common;

/**
 * The token contains information about the mongo host, 
 * port and userName. Since we do not do any user management, this token is the 
 * only way to map requests to the host, port and username.
 *
 * @author Rachit Mittal
 * 
 */
public class UserToken {

	private String tokenID;
	private String mongoHost;
	private int mongoPort;
	private String userName;

	/**
	 * Creates a token to represent the host, port and userName
	 *
	 * @param mongoHost
	 *            mongo Db IP Addresss.
	 * @param mongoPort
	 *            mongo Db Port
	 * @param userName
	 *            : Name of User.
	 */
	public UserToken(String mongoHost, int mongoPort, String userName) {
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
