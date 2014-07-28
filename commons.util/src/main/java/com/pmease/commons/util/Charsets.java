package com.pmease.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

public class Charsets extends org.apache.commons.io.Charsets {

	/**
	 * Read leading information of specified stream until the charset is detected.
	 * 
	 * @param contentStream
	 *			stream to be read for charset detection
	 * @return
	 * 			detected charset, or <tt>null</tt> if charset can not be detected or input 
	 * 			stream represents binary data
	 */
	public static @Nullable Charset detectFrom(InputStream contentStream) {
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
	 * 			charset of the content, or <tt>null</tt> if charset can not be detected or 
	 * 			content represents binary data
	 */
	public static @Nullable Charset detectFrom(byte[] contentBytes) {
		try {
			return UniversalEncodingDetector.detect(contentBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isBinary(InputStream contentStream) {
		try {
			return UniversalEncodingDetector.isBinary(contentStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isBinary(byte[] contentBytes) {
		try {
			return UniversalEncodingDetector.isBinary(contentBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
