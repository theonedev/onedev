package io.onedev.server.web.util;

import org.apache.tika.mime.MimeTypes;
import org.jspecify.annotations.Nullable;

public class MimeUtils {

	public static String sanitize(@Nullable String mediaType) {
		if (mediaType == null || mediaType.startsWith("image/svg")
				|| mediaType.equals("application/xml")) {
			return MimeTypes.OCTET_STREAM;
		} else if (mediaType.startsWith("image/") || mediaType.startsWith("video/")
				|| mediaType.equals("application/json")) {
			return mediaType;
		} else if (mediaType.startsWith("text/")) {
			return MimeTypes.PLAIN_TEXT;
		} else {
			return MimeTypes.OCTET_STREAM;
		}
	}
}
