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

// TODO SessionMongoInstnaceProvider used for now. this file is used in testing.
// Modify by reading from parameters an app.config file.
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
