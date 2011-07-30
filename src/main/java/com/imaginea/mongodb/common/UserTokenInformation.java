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
