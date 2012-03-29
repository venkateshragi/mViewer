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
package com.imaginea.mongodb.requestdispatchers;

import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.utils.FileUtil;
import com.imaginea.mongodb.services.GridFSService;
import com.imaginea.mongodb.services.GridFSServiceImpl;
import com.mongodb.DBObject;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Defines handlers for performing CRUD operations on files stored in GridFS.
 *
 * @author Srinath Anantha
 * @since Dec 3, 2008
 */
@Path("/{dbName}/{bucketName}/gridfs")
public class GridFSRequestDispatcher extends BaseRequestDispatcher {
    private final static Logger logger = Logger.getLogger(GridFSRequestDispatcher.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("create")
    public String createGridFSStore(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName,
                                    @QueryParam("dbInfo") final String dbInfo, @Context final HttpServletRequest request) {
        String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
            public Object execute() throws Exception {
                GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
                String result = gridFSService.createStore(dbName, bucketName);
                return result;
            }
        });
        return response;
    }

    /**
     * Request handler for getting the list of files stored in GridFS of specified database.
     *
     * @param dbName     Name of Database
     * @param bucketName Name of GridFS Bucket
     * @param dbInfo     Mongo Db Configuration provided by user to connect to.
     * @param request    Get the HTTP request context to extract session parameters
     * @returns JSON representation of list of all files as a String.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getfiles")
    public String getFileList(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName,
                              @QueryParam("dbInfo") final String dbInfo, @Context final HttpServletRequest request) {
        String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
            public Object execute() throws Exception {
                GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
                ArrayList<DBObject> fileObjects = gridFSService.getFileList(dbName, bucketName);
                return fileObjects;
            }
        });
        return response.replace("\\", "").replace("\"{", "{").replace("}\"", "}");
    }

    /**
     * Request handler for retrieving the specified file stored in GridFS.
     *
     * @param dbName         Name of Database
     * @param bucketName     Name of GridFS Bucket
     * @param id             ObjectId of the file to be retrieved
     * @param download       is download request
     * @param dbInfo         Mongo Db Configuration provided by user to connect to.
     * @param servletRequest Get the HTTP request context to extract session parameters
     * @returns Requested multipartfile for viewing or download based on 'download' param.
     */
    @GET
    @Path("getfile")
    public Response getFile(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName, @QueryParam("id") final String id,
                            @QueryParam("download") final boolean download, @QueryParam("dbInfo") final String dbInfo,
                            @Context final HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse) {
        GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
        File fileObject = null;
        try {
            fileObject = gridFSService.getFile(dbName, bucketName, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contentType = FileUtil.getContentType(fileObject);
        Response.ResponseBuilder response = Response.ok(fileObject, contentType);
        if (download == true) {
            response.header("Content-Disposition", "attachment; filename=" + fileObject.getName());
        } else {
            response.header("Content-Disposition", "filename=" + fileObject.getName());
        }
        return response.build();
    }

    /**
     * Request handler for uploading a file to GridFS.
     *
     * @param dbName         Name of Database
     * @param bucketName     Name of GridFS Bucket
     * @param fileDialogData formDataBodyPart representing the containing form
     * @param formData       formDataBodyPart of the uploaded file
     * @param inputStream    inputStream of the uploaded file
     * @param dbInfo         Mongo Db Configuration provided by user to connect to.
     * @param request        HTTP request context to extract session parameters
     * @returns Success message with additional file details such as name, size,
     * download url & deletion url as JSON Array string.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("uploadfile")
    public String uploadFile(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName,
                             @FormDataParam("addFileDialog") final FormDataBodyPart fileDialogData,
                             @FormDataParam("files") final FormDataBodyPart formData,
                             @FormDataParam("files") final InputStream inputStream,
                             @QueryParam("dbInfo") final String dbInfo, @Context final HttpServletRequest request) {

        String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
            public Object execute() throws Exception {
                GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
                return gridFSService.insertFile(dbName, bucketName, dbInfo, inputStream, formData);
            }
        }, false);
        return response;
    }

    /**
     * Request handler for dropping a file from GridFS.
     *
     * @param dbName     Name of Database
     * @param bucketName Name of GridFS Bucket
     * @param _id        Object id of file to be deleted
     * @param dbInfo     Mongo Db Configuration provided by user to connect to.
     * @param request    Get the HTTP request context to extract session parameters
     * @returns Status message.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("dropfile")
    public String dropFile(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName,
                           @QueryParam("id") final String _id, @QueryParam("dbInfo") final String dbInfo,
                           @Context final HttpServletRequest request) {

        String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
            public Object execute() throws Exception {
                GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
                String result = null;
                if ("".equals(_id)) {
                    UndefinedDocumentException e = new UndefinedDocumentException("File Data Missing in Request Body");
                    result = formErrorResponse(logger, e);
                } else {
                    ObjectId id = new ObjectId(_id);
                    result = gridFSService.deleteFile(dbName, bucketName, id);
                }
                return result;
            }
        });
        return response;
    }

    /**
     * Request handler for dropping all files from a GridFS bucket.
     *
     * @param dbName     Name of Database
     * @param bucketName Name of GridFS Bucket
     * @param dbInfo     Mongo Db Configuration provided by user to connect to.
     * @param request    Get the HTTP request context to extract session parameters
     * @return String with Status of operation performed.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("dropbucket")
    public String dropBucket(@PathParam("dbName") final String dbName, @PathParam("bucketName") final String bucketName,
                             @QueryParam("dbInfo") final String dbInfo, @Context final HttpServletRequest request) {
        String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
            public Object execute() throws Exception {
                GridFSService gridFSService = new GridFSServiceImpl(dbInfo);
                String result = gridFSService.dropBucket(dbName, bucketName);
                return result;
            }
        });
        return response;
    }
}
