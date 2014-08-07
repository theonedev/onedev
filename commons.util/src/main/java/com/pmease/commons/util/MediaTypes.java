package com.pmease.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;

public class MediaTypes {

	private static final Tika tika = new Tika();

	private MediaTypes() {}
	
	public static boolean isXML(MediaType mediaType) {
		return MediaType.APPLICATION_XML.equals(mediaType)
				|| "text/xml".equals(mediaType.toString())
				|| mediaType.getSubtype().endsWith("+xml");
	}
	
	public static boolean isJson(MediaType mediaType) {
		return mediaType.equals(MediaType.application("json"))
				|| mediaType.getSubtype().endsWith("json");
	}
	
	public static boolean isText(MediaType mediaType) {
		return mediaType.getType().equalsIgnoreCase("text")
				|| isXML(mediaType)
				|| isJson(mediaType)
				|| mediaType.equals(MediaType.application("x-sh"))
				|| mediaType.equals(MediaType.application("javascript"))
				|| mediaType.equals("x-httpd-jsp");
	}
	
	public static boolean isImage(MediaType mediaType) {
		return mediaType.getType().equals("image");
	}

	public static boolean isSafeInline(MediaType type) {
	    return isText(type) || isImage(type);
	}

	public static MediaType detectFrom(byte[] contentBytes, @Nullable String fileName) {
		String type = tika.detect(contentBytes, fileName);
		MediaType mediaType;
		if (MimeType.isValid(type))
			mediaType = MediaType.parse(type);
		else
			mediaType = MediaType.OCTET_STREAM;

		if (mediaType.equals(MediaType.OCTET_STREAM)) {
			Charset charset = Charsets.detectFrom(contentBytes);
			if (charset != null)
				mediaType = MediaType.TEXT_PLAIN;
		}
		
		return mediaType;
	}

	public static MediaType detectFrom(InputStream contentStream, @Nullable String fileName) {
		try {
			String type = tika.detect(contentStream, fileName);
			if (MimeType.isValid(type))
				return MediaType.parse(type);
			else
				return MediaType.OCTET_STREAM;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
