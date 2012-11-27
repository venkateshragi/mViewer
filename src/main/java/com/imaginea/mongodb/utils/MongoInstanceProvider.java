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
package com.imaginea.mongodb.utils;

import com.mongodb.Mongo;

/**
 * Defines the contract for implementations which provide the notion of current
 * mongo instance. For hibernate fans, this is like CurrentSessionContext.
 *
 * @author Rachit Mittal
 */
public interface MongoInstanceProvider {

    /**
     * Returns the current mongo instance, the meaning of current depends on the
     * actual implementation class
     *
     * @return a Mongo Instance
     */
    public Mongo getMongoInstance();

}
