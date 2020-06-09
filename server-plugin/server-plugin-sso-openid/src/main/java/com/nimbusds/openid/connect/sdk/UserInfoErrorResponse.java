package com.nimbusds.openid.connect.sdk;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ErrorResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerTokenError;


/**
 * UserInfo error response.
 *
 * <p>Standard OAuth 2.0 Bearer Token errors:
 *
 * <ul>
 *     <li>{@link com.nimbusds.oauth2.sdk.token.BearerTokenError#MISSING_TOKEN}
 *     <li>{@link com.nimbusds.oauth2.sdk.token.BearerTokenError#INVALID_REQUEST}
 *     <li>{@link com.nimbusds.oauth2.sdk.token.BearerTokenError#INVALID_TOKEN}
 *     <li>{@link com.nimbusds.oauth2.sdk.token.BearerTokenError#INSUFFICIENT_SCOPE}
 * </ul>
 *
 * <p>Example HTTP response:
 *
 * <pre>
 * HTTP/1.1 401 Unauthorized
 * WWW-Authenticate: Bearer realm="example.com",
 *                   error="invalid_token",
 *                   error_description="The access token expired"
 * </pre>
 *
 * <p>Related specifications:
 *
 * <ul>
 *     <li>OpenID Connect Core 1.0, section 5.3.3.
 *     <li>OAuth 2.0 Bearer Token Usage (RFC 6750), section 3.1.
 * </ul>
 */
@Immutable
public class UserInfoErrorResponse
	extends UserInfoResponse
	implements ErrorResponse {


	/**
	 * Gets the standard errors for a UserInfo error response.
	 *
	 * @return The standard errors, as a read-only set.
	 */
	public static Set<BearerTokenError> getStandardErrors() {
		
		Set<BearerTokenError> stdErrors = new HashSet<>();
		stdErrors.add(BearerTokenError.MISSING_TOKEN);
		stdErrors.add(BearerTokenError.INVALID_REQUEST);
		stdErrors.add(BearerTokenError.INVALID_TOKEN);
		stdErrors.add(BearerTokenError.INSUFFICIENT_SCOPE);

		return Collections.unmodifiableSet(stdErrors);
	}


	/**
	 * The underlying bearer token error.
	 */
	private final BearerTokenError error;


	/**
	 * Creates a new UserInfo error response. No OAuth 2.0 bearer token
	 * error is specified.
	 */
	private UserInfoErrorResponse() {

		error = null;
	}
	

	/**
	 * Creates a new UserInfo error response.
	 *
	 * @param error The OAuth 2.0 bearer token error. Should match one of 
	 *              the {@link #getStandardErrors standard errors} for a 
	 *              UserInfo error response. Must not be {@code null}.
	 */
	public UserInfoErrorResponse(final BearerTokenError error) {

		if (error == null)
			throw new IllegalArgumentException("The error must not be null");

		this.error = error;
	}


	@Override
	public ErrorObject getErrorObject() {

		return error;
	}


	/**
	 * Returns the HTTP response for this UserInfo error response.
	 *
	 * <p>Example HTTP response:
	 *
	 * <pre>
	 * HTTP/1.1 401 Unauthorized
	 * WWW-Authenticate: Bearer realm="example.com",
	 *                   error="invalid_token",
	 *                   error_description="The access token expired"
	 * </pre>
	 *
	 * @return The HTTP response matching this UserInfo error response.
	 */
	@Override
	public HTTPResponse toHTTPResponse() {

		HTTPResponse httpResponse;

		if (error.getHTTPStatusCode() > 0)
			httpResponse = new HTTPResponse(error.getHTTPStatusCode());
		else
			httpResponse = new HTTPResponse(HTTPResponse.SC_BAD_REQUEST);

		// Add the WWW-Authenticate header
		if (error != null)
			httpResponse.setWWWAuthenticate(error.toWWWAuthenticateHeader());

		return httpResponse;
	}


	/**
	 * Parses a UserInfo error response from the specified HTTP response
	 * {@code WWW-Authenticate} header.
	 *
	 * @param wwwAuth The {@code WWW-Authenticate} header value to parse. 
	 *                Must not be {@code null}.
	 *
	 * @throws ParseException If the {@code WWW-Authenticate} header value 
	 *                        couldn't be parsed to a UserInfo error 
	 *                        response.
	 */
	public static UserInfoErrorResponse parse(final String wwwAuth)
		throws ParseException {

		BearerTokenError error = BearerTokenError.parse(wwwAuth);

		return new UserInfoErrorResponse(error);
	}
	
	
	/**
	 * Parses a UserInfo error response from the specified HTTP response.
	 *
	 * <p>Note: The HTTP status code is not checked for matching the error
	 * code semantics.
	 *
	 * @param httpResponse The HTTP response to parse. Its status code must
	 *                     not be 200 (OK). Must not be {@code null}.
	 *
	 * @throws ParseException If the HTTP response couldn't be parsed to a 
	 *                        UserInfo error response.
	 */
	public static UserInfoErrorResponse parse(final HTTPResponse httpResponse)
		throws ParseException {
		
		httpResponse.ensureStatusCodeNotOK();

		String wwwAuth = httpResponse.getWWWAuthenticate();
		
		if (StringUtils.isNotBlank(wwwAuth))
			return parse(wwwAuth);

		return new UserInfoErrorResponse();
	}
}
