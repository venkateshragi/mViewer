package com.imaginea.mongodb.requestdispatchers;

import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.imaginea.mongodb.services.DocumentService;
import com.imaginea.mongodb.services.DocumentServiceImpl;

/**
 * Provides ServiceProvider functions that implement service provider interface.
 * 
 * @author Rachit Mittal
 * @since 2 Aug 2011
 * 
 */
public class ServiceProvider {

    /**
     * Return implementation provider of serviceClass interface
     * 
     * @param serviceClass
     * @return Implementation
     * @throws Exception
     */
    public static Class<?> getServiceImpl(Class<?> serviceClass)
            throws Exception {
        Class<?> serviceImpl = null;
        if (serviceClass.newInstance() instanceof DatabaseService) {
            return DatabaseServiceImpl.class;
        } else if (serviceClass.newInstance() instanceof CollectionService) {
            return CollectionServiceImpl.class;
        }
        if (serviceClass.newInstance() instanceof DocumentService) {
            return DocumentServiceImpl.class;
        }
        return serviceImpl;

    }
}
