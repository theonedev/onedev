package io.onedev.server.util;

import org.apache.commons.codec.binary.Base64;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class HttpUtils {

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
