package io.onedev.server.plugin.sso.discord.oauth2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import org.apache.http.entity.ContentType;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String AUTHORIZE_ENDPOINT = "https://discord.com/api/oauth2/authorize";
	private static final String USER_AGENT = "onedev";
	
	private String clientId;
	private String scope;
	private String callbackURI;
	private String state;
	
	private enum Method {
		GET,
		POST
	}
	
	public Request(String clientId, String scope, String callbackURI) {
		this.clientId = clientId;
		this.scope = scope;
		this.callbackURI = callbackURI;
		this.state = "DISCORD-" + UUID.randomUUID().toString();
	}
	
	public String getState() {
		return state;
	}

	public String getAuthorizationURI() {
		String authURI = AUTHORIZE_ENDPOINT +
				"?response_type=code" + 
				"&client_id=" + clientId +
				"&scope=" + scope +
				"&redirect_uri=" + callbackURI +
				"&state=" + state +
				"&prompt=none";
		
		return authURI;
	}
	
	public AccessTokenResponse getToken(String clientSecret, String code) throws IOException {
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("client_id", clientId);
		requestParams.put("client_secret", clientSecret);
		requestParams.put("grant_type", "authorization_code");
		requestParams.put("code", code);
		requestParams.put("redirect_uri", callbackURI);
		
		String requestBody = Utils.serializeParameters(requestParams);
		
		HttpURLConnection httpConn = openConnection(Method.POST, AccessTokenResponse.ENDPOINT, requestBody, null);
		return new AccessTokenResponse(readContent(httpConn), httpConn.getResponseCode());
	}
	
	public UserInfoResponse getUserInfo(AccessTokenResponse accessTokenResponse) throws IOException {
		HttpURLConnection httpConn = openConnection(Method.GET, UserInfoResponse.ENDPOINT, null, accessTokenResponse);
		return new UserInfoResponse(readContent(httpConn), httpConn.getResponseCode());
	}
	
	public UserGuildsResponse getUserGuilds(AccessTokenResponse accessTokenResponse) throws IOException {
		HttpURLConnection httpConn = openConnection(Method.GET, UserGuildsResponse.ENDPOINT, null, accessTokenResponse);
		return new UserGuildsResponse(readContent(httpConn), httpConn.getResponseCode());
	}
	
	private static HttpURLConnection openConnection(Method method, String endpoint, @Nullable String query, @Nullable AccessTokenResponse accessTokenResponse) throws IOException {
		URL finalURL = new URL(endpoint);
		
		if (query != null && method.equals(Method.GET)) {
			// Append query string
			StringBuilder sb = new StringBuilder(finalURL.toString());
			sb.append('?');
			sb.append(query);

			try {
				finalURL = new URL(sb.toString());
			} catch (MalformedURLException e) {
				throw new IOException("Couldn't append query string: " + e.getMessage(), e);
			}
		}
		
		HttpURLConnection httpConn = (HttpURLConnection)finalURL.openConnection();
		httpConn.setDoOutput(true);
		httpConn.setRequestProperty("User-Agent", USER_AGENT);
		
		if (accessTokenResponse != null && accessTokenResponse.hasValidAccessToken())
			httpConn.setRequestProperty("Authorization", accessTokenResponse.getAuthorizationProperty());
		
		httpConn.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
		httpConn.setRequestMethod(method.name());
		
		if (method.equals(Method.POST)) {
			httpConn.setRequestProperty("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
			
			if (query != null) {
				OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
				writer.write(query);
				writer.close();
			}
		}
		
		return httpConn;
	}
	
	private static String readContent(HttpURLConnection httpConn) throws IOException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		StringBuffer response = new StringBuffer();

		try {
			String inputLine;
			while ((inputLine = buffer.readLine()) != null) {
				response.append(inputLine);
			}
			buffer.close();
		} catch (IOException e) {
			throw e;
		} finally {
			httpConn.disconnect();
		}
		
		return response.toString();
	}
}
