package io.onedev.server.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;

public class OAuthUtils {

	private static final Map<String, Pair<AccessToken, Date>> accessTokenCache = new ConcurrentHashMap<>();
	
	public static String getErrorMessage(ErrorObject error) {
		if ("redirect_uri_mismatch".equals(error.getCode())) {
			return "Redirect uri mismatch: make sure the server url specified in system setting is the same as "
					+ "root part of the authorization callback url at authorization server side";
		} else {
			List<String> details = new ArrayList<>();
			if (error.getCode() != null) 
				details.add("code: " + error.getCode());
			if (error.getDescription() != null)
				details.add("description: " + error.getDescription());
			if (error.getHTTPStatusCode() != 0)
				details.add("http status code: " + error.getHTTPStatusCode());
			
			return StringUtils.join(details, ", ");
		}
	}
	
	public static String getAccessToken(String tokenEndpoint, String clientId, String clientSecret, 
			String refreshToken) {
		Pair<AccessToken, Date> cacheEntry = accessTokenCache.get(refreshToken);
		if (cacheEntry == null) {
			cacheEntry = new Pair<>(
					generateAccessToken(tokenEndpoint, clientId, clientSecret, refreshToken), 
					new Date());
			accessTokenCache.put(refreshToken, cacheEntry);
		} else {
			long lifetime = cacheEntry.getLeft().getLifetime();
			if (lifetime != 0 && new DateTime(cacheEntry.getRight()).plusSeconds((int)lifetime).isBeforeNow()) {
				cacheEntry = new Pair<>(
						generateAccessToken(tokenEndpoint, clientId, clientSecret, refreshToken), 
						new Date());
				accessTokenCache.put(refreshToken, cacheEntry);
			}
		}
		return cacheEntry.getLeft().getValue();
	}

	public static AccessToken generateAccessToken(String tokenEndpoint, 
			String clientId, String clientSecret, String refreshTokenValue) {
		com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken = 
				new com.nimbusds.oauth2.sdk.token.RefreshToken(refreshTokenValue);
		AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshToken);
		
		ClientAuthentication clientAuth = new ClientSecretBasic(
				new ClientID(clientId), new Secret(clientSecret));

		TokenResponse response;
		try {
			TokenRequest request = new TokenRequest(new URI(tokenEndpoint), clientAuth, refreshTokenGrant);
			response = TokenResponse.parse(request.toHTTPRequest().send());
		} catch (ParseException | URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}

		if (response.indicatesSuccess()) {
			return response.toSuccessResponse().getTokens().getAccessToken();
		} else {
		    TokenErrorResponse errorResponse = response.toErrorResponse();
		    throw new ExplicitException(getErrorMessage(errorResponse.getErrorObject()));
		}
		
	}
	
}
