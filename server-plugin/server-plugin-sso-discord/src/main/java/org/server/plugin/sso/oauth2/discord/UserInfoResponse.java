package org.server.plugin.sso.oauth2.discord;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoResponse extends Response {
	public static final String ENDPOINT = "https://discord.com/api/users/@me";
	
	private String message;

	public UserInfoResponse(String content, int responseCode) {
		super(content, responseCode);
		
		try {
			JSONObject jsonObject = getContentAsJSONObject();
			
			if (!isOK()) {
				this.message = (String) Utils.getValue(jsonObject, "message");
			}
		} catch (JSONException e) {
		}
	}

	public String getMessage() {
		return message;
	}
}
