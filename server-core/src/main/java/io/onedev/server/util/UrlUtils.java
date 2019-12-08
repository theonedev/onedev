package io.onedev.server.util;

import java.util.regex.Pattern;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;

public class UrlUtils {

	private static final Pattern SCHEME_PATTERN = Pattern.compile("(mailto:|^\\w+\\:\\/\\/)");
	
	public static boolean isRelative(String url) {
		return !url.startsWith("/") && !SCHEME_PATTERN.matcher(url).find();
	}
	
	public static String trimHashAndQuery(String url) {
		url = StringUtils.substringBeforeLast(url, "#");
		url = StringUtils.substringBeforeLast(url, "?");
		return url;
	}
	
	public static String describe(String url) {
		if (url.startsWith("http://"))
			return url.substring("http://".length());
		if (url.startsWith("https://"))
			return url.substring("https://".length());
		if (url.contains("/"))
			url = StringUtils.substringAfterLast(url, "/");
		url = StringUtils.substringBeforeLast(url, ".");
		if (url.contains("."))
			url = StringUtils.substringAfterLast(url, ".");
		url = StringUtils.substringBefore(StringUtils.replaceChars(url, "-_", "  "), "?");
		return WordUtils.capitalize(url);
	}

}
