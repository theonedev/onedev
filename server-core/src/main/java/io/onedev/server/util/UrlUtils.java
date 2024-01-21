package io.onedev.server.util;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.StringMode;
import org.apache.wicket.util.encoding.UrlDecoder;
import org.apache.wicket.util.encoding.UrlEncoder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

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

	public static String encodePath(String url) {
		return UrlEncoder.PATH_INSTANCE.encode(url, StandardCharsets.UTF_8);
	}
	
	public static String decodePath(String url) {
		return UrlDecoder.PATH_INSTANCE.decode(url, StandardCharsets.UTF_8.name());
	}
	
	public static String encodeQuery(String query) {
		return UrlEncoder.QUERY_INSTANCE.encode(query, StandardCharsets.UTF_8);
	}
	
	public static String decodeQuery(String query) {
		return UrlDecoder.QUERY_INSTANCE.decode(query, StandardCharsets.UTF_8.name());
	}
	
	public static String makeRelative(String url) {
		try {
			URIBuilder builder = new URIBuilder(url);
			builder.setScheme(null);
			builder.setHost(null);
			return builder.build().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toString(Url url) {
		return StringUtils.stripEnd(url.toString(StringMode.FULL), "/");
	}
	
	public static String getServer(String url) {
		try {
			var parsedUrl = new URL(url);
			if (parsedUrl.getPort() != -1)
				return parsedUrl.getHost() + ":" + parsedUrl.getPort();
			else
				return parsedUrl.getHost();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
