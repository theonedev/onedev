package io.onedev.server.plugin.sso.discord.oauth2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
	private static final int HTTP_OK = 200;
	
	private String content;
	private int responseCode;
	
	public Response(String content, int responseCode) {
		this.content = content;
		this.responseCode = responseCode;
	}
	
	public int getResponseCode() {
		return this.responseCode;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public JSONObject getContentAsJSONObject() throws JSONException {
		return new JSONObject(content);
	}
	
	public JSONArray getContentAsJSONArray() throws JSONException {
		return new JSONArray(content);
	}
	
	public boolean isOK() {
		return responseCode == HTTP_OK;
	}
}
