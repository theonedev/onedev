package com.pmease.gitop.web.util;

import org.apache.tika.mime.MimeType;

public class MimeTypeUtils {

	private MimeTypeUtils() {}
	
	public static boolean isTextType(MimeType type) {
		return type.getType().getType().equalsIgnoreCase("text")
                || type.getType().getSubtype().equalsIgnoreCase("text");
	}
	
	public static boolean isXMLType(final MimeType type) {
        return type.getType().getType().equalsIgnoreCase("xml")
                || type.getType().getSubtype().endsWith("xml");
    }
	
	public static boolean isImageType(final MimeType type) {
    	return type.getType().getType().equalsIgnoreCase("image")
    			|| type.getType().getSubtype().endsWith("image");
    			
    }
}
