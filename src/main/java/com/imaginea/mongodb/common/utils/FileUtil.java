package com.imaginea.mongodb.common.utils;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Collection;

/**
 * Author: Srinath Anantha
 * Date: Mar 16, 2012
 * Time: 3:57:38 PM
 */
public class FileUtil {

    static {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    public static String getContentType(File file) {
        Collection types = MimeUtil.getMimeTypes(file, new MimeType(MediaType.APPLICATION_OCTET_STREAM));
        return MimeUtil.getMostSpecificMimeType(types).toString();
    }
}
