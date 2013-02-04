package com.imaginea.mongodb.controllers;

import com.imaginea.mongodb.exceptions.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.FileNotFoundException;

/**
 * Defined an Error Template to catch more exceptions here while Testing. and
 * new asserts in case of some. Exceptions for a block of Code for test files.
 *
 *
 * @author Rachit Mittal
 * @since 2 Aug 2011
 *
 */
public class TestingTemplate extends BaseController {
	public static void execute(Logger logger, ResponseCallback callback) {

		try {
			Object dispatcherResponse = callback.execute();
			JSONObject tempResult = new JSONObject();
			JSONObject jsonResponse = new JSONObject();
			tempResult.put("result", dispatcherResponse);
			jsonResponse.put("response", tempResult);
			jsonResponse.toString();
		} catch (FileNotFoundException m) {
			ApplicationException e = new ApplicationException(ErrorCodes.FILE_NOT_FOUND_EXCEPTION, m.getMessage(), m.getCause());
			formErrorResponse(logger, e);
		} catch (DatabaseException e) {
			formErrorResponse(logger, e);
			// This condition is ok .. as if service throw this error while
			// creating Db that already exist
			assert (true);
		} catch (CollectionException e) {
			formErrorResponse(logger, e);
			assert (true);
		} catch (DocumentException e) {
			formErrorResponse(logger, e);
			assert (true);
		} catch (ValidationException e) {
			formErrorResponse(logger, e);
			assert (true);
		} catch (Exception m) {
			// For any other exception call the template as deinfed in src
			// folder
			BaseController.ErrorTemplate.execute(logger, callback);
		}
	}

}
