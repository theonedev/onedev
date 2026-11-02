package io.onedev.server.oauth;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

import io.onedev.server.model.OAuthApplication;

public class OAuthProtocol {

	public static final long ACCESS_TOKEN_SECONDS = 3600;
	public static final long REFRESH_TOKEN_SECONDS = 30 * 24 * 3600;

	public static String hash(String value) {
		return DigestUtils.sha256Hex(value);
	}

	public static boolean matches(String value, String hash) {
		return hash != null && MessageDigest.isEqual(hash(value).getBytes(StandardCharsets.US_ASCII),
				hash.getBytes(StandardCharsets.US_ASCII));
	}

	public static void checkSingleParameters(Map<String, List<String>> parameters) throws ParseException {
		for (var entry: parameters.entrySet()) {
			if (entry.getValue().size() != 1)
				throw new ParseException("Repeated request parameter", OAuth2Error.INVALID_REQUEST);
		}
	}

	public static boolean isValidRedirectUri(String value, boolean confidential) {
		try {
			URI uri = URI.create(value);
			if (!uri.isAbsolute() || uri.getFragment() != null || uri.getUserInfo() != null || value.contains("*"))
				return false;
			if ("https".equals(uri.getScheme()))
				return uri.getHost() != null;
			if ("http".equals(uri.getScheme()))
				return uri.getHost() != null && List.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
			// Private-use schemes are limited to public native applications (RFC 8252).
			return !confidential && uri.getScheme().contains(".") && !uri.isOpaque();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean hasRegisteredRedirect(OAuthApplication application, AuthorizationRequest request) {
		var redirect = request.getRedirectionURI();
		if (redirect == null)
			return false;
		if (application.getRedirectUriList().contains(redirect.toString()))
			return true;
		// Native clients bind an available loopback port at runtime (RFC 8252, section 7.3).
		// All other URI components still have to match the registered value exactly.
		if (!application.isConfidential() && isLoopbackHttp(redirect)
				&& redirect.getPort() > 0 && redirect.getPort() <= 65535) {
			for (var registeredValue: application.getRedirectUriList()) {
				var registered = URI.create(registeredValue);
				if (isLoopbackHttp(registered) && registered.getHost().equals(redirect.getHost())
						&& Objects.equals(registered.getRawPath(), redirect.getRawPath())
						&& Objects.equals(registered.getRawQuery(), redirect.getRawQuery()))
					return true;
			}
		}
		return false;
	}

	private static boolean isLoopbackHttp(URI uri) {
		return "http".equals(uri.getScheme()) && uri.getRawUserInfo() == null && uri.getRawFragment() == null
				&& ("127.0.0.1".equals(uri.getHost()) || "[::1]".equals(uri.getHost()));
	}

	public static void validateAuthorization(AuthorizationRequest request) throws ParseException {
		if (!new ResponseType(ResponseType.Value.CODE).equals(request.getResponseType()))
			throw new ParseException("Only the authorization code flow is supported", OAuth2Error.UNSUPPORTED_RESPONSE_TYPE);
		if (request.getResponseMode() != null && !ResponseMode.QUERY.equals(request.getResponseMode()))
			throw new ParseException("Only query responses are supported", OAuth2Error.INVALID_REQUEST);
		if (request.specifiesRequestObject() || request.getResources() != null
				|| request.getAuthorizationDetails() != null || request.getDPoPJWKThumbprintConfirmation() != null)
			throw new ParseException("Unsupported authorization request extension", OAuth2Error.INVALID_REQUEST);
		if (!CodeChallengeMethod.S256.equals(request.getCodeChallengeMethod())
				|| request.getCodeChallenge() == null
				|| !request.getCodeChallenge().getValue().matches("[A-Za-z0-9_-]{43}"))
			throw new ParseException("PKCE with S256 is required", OAuth2Error.INVALID_REQUEST);
		for (String parameter: List.of("max_age", "claims", "acr_values", "id_token_hint")) {
			if (request.getCustomParameter(parameter) != null)
				throw new ParseException("Unsupported authentication parameter: " + parameter, OAuth2Error.INVALID_REQUEST);
		}
		var nonce = request.getCustomParameter("nonce");
		if (nonce != null && (nonce.size() != 1 || nonce.get(0).length() > 255))
			throw new ParseException("Invalid nonce", OAuth2Error.INVALID_REQUEST);
	}

	public static boolean verifyPkce(String challenge, CodeVerifier verifier) {
		return verifier != null && challenge != null
				&& verifier.getValue().matches("[A-Za-z0-9._~-]{43,128}")
				&& CodeChallenge.compute(CodeChallengeMethod.S256, verifier).getValue().equals(challenge);
	}

	public static Long grantId(String token) {
		try {
			int separator = token.indexOf('.');
			return separator > 0 ? Long.valueOf(token.substring(0, separator)) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
