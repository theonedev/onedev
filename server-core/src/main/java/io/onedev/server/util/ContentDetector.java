package io.onedev.server.util;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ContentDetector {
	
	private static final Tika tika = new Tika();
	
	/**
	 * Read leading information of specified content bytes to detect content charset.
	 *  
	 * @param bytes
	 * 			content to be detected
	 * @return
	 * 			charset of the content, or <tt>null</tt> if charset can not be detected
	 */
	@Nullable
	public static Charset detectCharset(byte[] bytes) {
		if (bytes.length != 0) {
			var listener = new UniversalEncodingListener(new Metadata());
			var pos = 0;
			var lookAhead = 1024;
			while (true) {
				var left = bytes.length - pos;
				if (left < lookAhead) {
					listener.handleData(bytes, pos, left);
					break;
				} else {
					listener.handleData(bytes, pos, lookAhead);
					if (listener.isDone())
						break;
					else
						pos += lookAhead;
				}
			}
			return listener.dataEnd();
		} else {
			return null;
		}
	}
	
	public static boolean isBinary(byte[] contentBytes, @Nullable String fileName) {
		if (contentBytes.length == 0)
			return false;
		
		MediaType mediaType = detectMediaType(contentBytes, fileName);
		
		return !mediaType.getType().equalsIgnoreCase("text")
				&& !mediaType.equals(MediaType.application("rls-services+xml"))
				&& !mediaType.equals(MediaType.application("xhtml+xml"))
				&& !mediaType.equals(MediaType.APPLICATION_XML)
				&& !mediaType.equals(MediaType.application("x-bat"))
				&& !mediaType.equals(MediaType.application("x-tex"))
				&& !mediaType.equals(MediaType.application("json"))
				&& !mediaType.equals(MediaType.application("x-sh"))
				&& !mediaType.equals(MediaType.application("javascript"))
				&& !mediaType.equals(MediaType.application("x-httpd-jsp"))
				&& !mediaType.equals(MediaType.application("x-httpd-php"));
	}
	
	/**
	 * Get text from specified content bytes, optionally with help of file name.
	 * 
	 * @param contentBytes
	 * 			content bytes to construct text from
	 * @param fileName
	 * 			file name to help deciding if supplied content bytes represents text
	 * @return
	 * 			text representation of content bytes, or <tt>null</tt> if content 
	 * 			can not be converted to text
	 */
	@Nullable
	public static String convertToText(byte[] contentBytes, @Nullable String fileName) {
		if (!isBinary(contentBytes, fileName)) {
			Charset charset = detectCharset(contentBytes);
			if (charset != null)
				return new String(contentBytes, charset);
			else
				return new String(contentBytes);
		} else {
			return null;
		}
	}

	public static MediaType detectMediaType(byte[] contentBytes, @Nullable String fileName) {
		return MediaType.parse(tika.detect(contentBytes, fileName));
	}

	public static MediaType detectMediaType(InputStream contentStream, @Nullable String fileName) {
		try {
			return MediaType.parse(tika.detect(contentStream, fileName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
