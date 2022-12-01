package io.onedev.server.web.util;

import org.apache.tika.mime.MimeTypes;

public class MimeUtils {

	public static String sanitize(String mediaType) {
		if (mediaType.startsWith("image/") || mediaType.startsWith("video/") 
				|| mediaType.equals("application/json") || mediaType.equals("application/xml")) { 
			return mediaType;
		} else if (mediaType.startsWith("text/")) {
			return MimeTypes.PLAIN_TEXT;
		} else { 
			return MimeTypes.OCTET_STREAM;
		}
	}
}
