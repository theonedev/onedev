package io.onedev.server.util;

import java.util.Locale;

import org.apache.commons.codec.binary.Base64;

import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class HttpUtils {

	public static boolean isBot(HttpServletRequest request) {
		var userAgent = request.getHeader("User-Agent");
		if (userAgent == null)
			return false;
		userAgent = userAgent.toLowerCase(Locale.ROOT);
		return userAgent.contains("bot") || userAgent.contains("crawler")
				|| userAgent.contains("spider") || userAgent.contains("crawling");
	}

	@Nullable
	public static String getAuthBasicUser(HttpServletRequest request) {
		var auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.toLowerCase().startsWith("basic ")) {
			var authValue = substringAfter(auth, " ");
			var decodedAuthValue = new String(Base64.decodeBase64(authValue), UTF_8);
			return substringBefore(decodedAuthValue, ":");
		} else {
			return null;
		}
	}
	
}
