package io.onedev.server.util;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import org.jspecify.annotations.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ContentDetector {
	
	private static final int BUFSIZE = 1024;
	
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
			var input = new ByteArrayInputStream(bytes);
			input.mark(bytes.length);
			try {
				UniversalEncodingListener listener = new UniversalEncodingListener(new Metadata());

				byte[] b = new byte[BUFSIZE];
				int n = 0;
				int m = input.read(b);
				while (m != -1 && n < bytes.length && !listener.isDone()) {
					n += m;
					listener.handleData(b, 0, m);
					m = input.read(b, 0, Math.min(b.length, bytes.length - n));
				}
				return listener.dataEnd();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				input.reset();
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
