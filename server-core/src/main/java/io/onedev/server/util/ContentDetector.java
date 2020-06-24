package io.onedev.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

public class ContentDetector {
	
	private static final Tika tika = new Tika();

	/**
	 * Read leading information of specified stream until the charset is detected.
	 * 
	 * @param contentStream
	 *			stream to be read for charset detection
	 * @return
	 * 			detected charset, or <tt>null</tt> if charset can not be detected
	 */
	public static @Nullable Charset detectCharset(InputStream contentStream) {
		try {
			return UniversalEncodingDetector.detect(contentStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read leading information of specified content bytes to detect content charset.
	 *  
	 * @param contentBytes
	 * 			content to be detected
	 * @return
	 * 			charset of the content, or <tt>null</tt> if charset can not be detected
	 */
	public static @Nullable Charset detectCharset(byte[] contentBytes) {
		if (contentBytes.length != 0) {
			try {
				return UniversalEncodingDetector.detect(contentBytes);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
