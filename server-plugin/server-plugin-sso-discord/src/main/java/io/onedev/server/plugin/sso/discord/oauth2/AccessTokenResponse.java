package io.onedev.server.plugin.sso.discord.oauth2;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class AccessTokenResponse extends Response {
	public static final String ENDPOINT = "https://discord.com/api/oauth2/token";
	private static final String VALID_TOKEN_TYPE = "Bearer";
	private static final String DEFAULT_ERROR = "Unable to get access token";
	
	private String accessToken;
	private String tokenType;
	private int expiresIn;
	private String refreshToken;
	private String scope;
	private String error;

	public AccessTokenResponse(String content, int responseCode) {
		super(content, responseCode);
		
		try {
			JSONObject jsonObject = getContentAsJSONObject();
			
			if (isOK()) {
				this.accessToken = (String) Utils.getValue(jsonObject, "access_token");
				this.tokenType = (String) Utils.getValue(jsonObject, "token_type");
				this.expiresIn = (int) Utils.getValue(jsonObject, "expires_in");
				this.refreshToken = (String) Utils.getValue(jsonObject, "refresh_token");
				this.scope = (String) Utils.getValue(jsonObject, "scope");
			} else {
				this.error = (String) Utils.getValue(jsonObject, "error");
			}
		} catch (JSONException e) {
		}
	}
	
	public boolean hasValidAccessToken() {
		return StringUtils.isNotEmpty(accessToken) && StringUtils.equals(tokenType, VALID_TOKEN_TYPE);
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getError() {
		return StringUtils.isNotEmpty(error) ? error : DEFAULT_ERROR;
	}
	
	public String getAuthorizationProperty() {
		return tokenType + " " + accessToken;
	}
}
