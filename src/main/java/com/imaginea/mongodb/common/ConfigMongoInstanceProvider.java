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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Provides an Implementation for MongoInstanceProvider that provides instance
 * of Mongo Db after reading parameters from config file for performing
 * operations on MonogDb Resources {Db,collection,document}.
 * 
 * @author Rachit Mittal
 * 
 */

public class ConfigMongoInstanceProvider implements MongoInstanceProvider {

	private Mongo mongoInstance;

	private String mongoHost = "127.0.0.1"; // From app.config read
	private int mongoPort = 27017;

	/**
	 * Get the initial MongoIP and mongoPort from config file and returns a
	 * MongoInstance.
	 * 
	 * @throws MongoHostUnknownException
	 *             : Mongo Host Unknown
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	public ConfigMongoInstanceProvider() throws MongoHostUnknownException,
			IOException, FileNotFoundException {
		try {

			Properties prop = new Properties();
			String fileName = "mongo.config";
			InputStream is = new FileInputStream(fileName);
			prop.load(is);

			if (prop != null) {
				mongoHost = prop.getProperty("mongoHost");
				mongoPort = Integer.parseInt(prop.getProperty("mongoPort"));
				mongoInstance = new Mongo(mongoHost, mongoPort);
			}

		} catch (MongoException e) {
			throw new MongoHostUnknownException("HOST_UNKNOWN", e);
		}
	}

	/**
	 * 
	 * @return mongo Instance
	 */
	@Override
	public Mongo getMongoInstance() {
		return mongoInstance;
	}

}
