package io.onedev.server.plugin.sso.openid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import io.onedev.server.annotation.Editable;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Editable(name="Twitch", order=200, description = "Single sign on via twitch.tv")
public class TwitchConnector extends OpenIdConnector {

	private static final long serialVersionUID = 1L;

	public TwitchConnector() {
		setName("Twitch");
	}

	@Override
	public String getConfigurationDiscoveryUrl() {
		return super.getConfigurationDiscoveryUrl();
	}

	@Override
	protected ProviderMetadata discoverProviderMetadata() {
		return new ProviderMetadata(
				"https://id.twitch.tv/oauth2",
				"https://id.twitch.tv/oauth2/authorize?claims=" + URLEncoder.encode("{\"id_token\":{}, \"userinfo\":{\"email\": null,\"preferred_username\": null}}", StandardCharsets.UTF_8), 
				"https://id.twitch.tv/oauth2/token", 
				"https://id.twitch.tv/oauth2/userinfo");
	}

	@Override
	public String getGroupsClaim() {
		return super.getGroupsClaim();
	}

	@Override
	public boolean isManagingMemberships() {
		return false;
	}

	@Override
	protected String getBaseScope() {
		return "openid user:read:email";
	}

	@Override
	protected ClientAuthentication createTokenRequestAuthentication(ClientID id, Secret secret) {
		return new ClientSecretPost(id, secret);
	}

	@Override
	protected TokenResponse parseOIDCTokenResponse(HTTPResponse response) throws ParseException {
		JSONObject responseJson = response.getContentAsJSONObject();
		if (response.getStatusCode() == HTTPResponse.SC_OK) {
			Object scopeValue = responseJson.remove("scope");
			Scope scope;
			if (scopeValue instanceof JSONArray) {
				JSONArray scopeArray = (JSONArray) scopeValue;
				String[] scopeArrayString = scopeArray.stream().map(Object::toString).toArray(String[]::new);
				 scope = new Scope(scopeArrayString);
			} else if(scopeValue instanceof String) {
				String scopeString = (String) scopeValue;
				scope = new Scope(scopeString);
			} else {
				scope = new Scope();
			}
			OIDCTokenResponse token = OIDCTokenResponse.parse(responseJson);
			OIDCTokens oidcTokens = token.getOIDCTokens();
			BearerAccessToken accessToken = oidcTokens.getBearerAccessToken();
			BearerAccessToken fixedBearerToken = new BearerAccessToken(accessToken.getValue(), accessToken.getLifetime(), scope, accessToken.getIssuedTokenType());
			return new OIDCTokenResponse(new OIDCTokens(oidcTokens.getIDToken(), fixedBearerToken, oidcTokens.getRefreshToken()), token.getCustomParameters());
		} else {
			return TokenErrorResponse.parse(responseJson);
		}
	}
}
