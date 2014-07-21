package com.pmease.gitplex.web.util;

import java.util.Set;

import org.apache.tika.mime.MediaType;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class MediaTypeUtils {

	private MediaTypeUtils() {}
	
	public static final Set<MediaType> EXTRA_TEXT_TYPES = ImmutableSet.<MediaType>of(
			MediaType.application("x-sh"),
			MediaType.application("javascript"),
			MediaType.application("x-httpd-jsp")
			);
	
	public static boolean isXMLType(MediaType mediaType) {
		return Objects.equal(MediaType.APPLICATION_XML, mediaType)
				|| "text/xml".equals(mediaType.toString())
				|| mediaType.getSubtype().endsWith("+xml");
	}
	
	public static boolean isJsonType(MediaType mediaType) {
		return Objects.equal(mediaType, MediaType.application("json"))
				|| mediaType.getSubtype().endsWith("json");
	}
	
	public static boolean isTextType(MediaType mediaType) {
		return Objects.equal(mediaType, MediaType.TEXT_PLAIN)
				|| mediaType.getType().equalsIgnoreCase("text")
				|| isXMLType(mediaType)
				|| isJsonType(mediaType)
				|| EXTRA_TEXT_TYPES.contains(mediaType);
	}
	
	public static boolean isImageType(MediaType mediaType) {
		return mediaType.getType().equals("image");
	}
}
