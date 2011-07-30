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

package com.imaginea.mongodb.testsuite;
  
import java.io.IOException; 

import com.imaginea.mongodb.requestdispatchers.CollectionRequestDispatcherTest;
import com.imaginea.mongodb.requestdispatchers.DatabaseRequestDispatcherTest;
import com.imaginea.mongodb.requestdispatchers.DocumentRequestDispatcherTest;
import com.imaginea.mongodb.requestdispatchers.StatisticsRequestDispatcherTest;
import com.imaginea.mongodb.services.CollectionServiceImplTest;
import com.imaginea.mongodb.services.DatabaseServiceImplTest;
import com.imaginea.mongodb.services.DocumentServiceImplTest;
import com.imaginea.mongodb.services.servlet.UserLoginTest;
import com.imaginea.mongodb.services.servlet.UserLogoutTest;
 

import junit.framework.Test;
import junit.framework.TestSuite;
  
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DatabaseServiceImplTest.class,
		CollectionServiceImplTest.class, DocumentServiceImplTest.class,
		DatabaseRequestDispatcherTest.class,
		CollectionRequestDispatcherTest.class,
		DocumentRequestDispatcherTest.class, UserLoginTest.class,
		StatisticsRequestDispatcherTest.class, UserLogoutTest.class })
public class MongoViewerTestSuite {

	public static Test suite() throws IOException {
		TestSuite suite = new TestSuite(MongoViewerTestSuite.class.getName());
		 
		// Start Mongod
		Runtime run = Runtime.getRuntime();
		Process p = run.exec("c:\\mongo\\bin\\mongod");

		p.destroy();

		return suite;
	}

}
