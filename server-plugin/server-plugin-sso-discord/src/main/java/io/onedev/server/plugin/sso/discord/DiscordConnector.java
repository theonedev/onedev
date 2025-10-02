package io.onedev.server.plugin.sso.discord;

import static io.onedev.server.web.translation.Translation._T;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;
import org.json.JSONException;
import org.json.JSONObject;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.plugin.sso.discord.oauth2.AccessTokenResponse;
import io.onedev.server.plugin.sso.discord.oauth2.Request;
import io.onedev.server.plugin.sso.discord.oauth2.UserGuildsResponse;
import io.onedev.server.plugin.sso.discord.oauth2.UserInfoResponse;

@Editable(name="Discord", order=150, description="Single sign on via discord.com")
public class DiscordConnector extends SsoConnector {
	
	private static final long serialVersionUID = 1L;
	private static final String SESSION_ATTR_API_REQUEST = "DiscordApiRequest";
	
	private String clientId;
	private String clientSecret;
	private String serverId;
		
	@Editable(order=1000, description="OAuth2 Client information | CLIENT ID")
	@NotEmpty
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Editable(order=1100, description="OAuth2 Client information | CLIENT SECRET")
	@Password
	@NotEmpty
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	@Editable(order=1200, description="Provide server id (guild id) to restrict access only to server members")
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public String getButtonImageUrl() {
		return "/wicket/resource/" + DiscordConnector.class.getName() + "/discord.png";
	}

	@Override
	public SsoAuthenticated handleAuthResponse(String providerName) {
		HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
		DiscordAuthorizationCodeResponse codeResponse = new DiscordAuthorizationCodeResponse(request.getQueryString());
		
		if (codeResponse.hasValidCode()) {
			Request apiRequest = getCachedApiRequest();
			
			if (codeResponse.hasState(apiRequest.getState())) {
				try {
					AccessTokenResponse accessTokenResponse = apiRequest.getToken(getClientSecret(), codeResponse.getCode());
					
					if (accessTokenResponse.isOK() && accessTokenResponse.hasValidAccessToken()) {
						return processTokenResponse(accessTokenResponse);
					}
					
					throw new AuthenticationException(accessTokenResponse.getContent());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new AuthenticationException(_T("Invalid state response"));
			}
		} else {
			throw new AuthenticationException(codeResponse.getError());
		}
	}

	@Override
	public String buildAuthUrl(String providerName) {
		Request apiRequest = new Request(getClientId(), getScopes(), getCallbackUri(providerName).toString());
		Session.get().setAttribute(SESSION_ATTR_API_REQUEST, apiRequest);
		
		String authURI = apiRequest.getAuthorizationURI();
		return authURI;
	}
	
	private SsoAuthenticated processTokenResponse(AccessTokenResponse accessTokenResponse) {
		try {
			final boolean bCheckGuilds = !StringUtils.isEmpty(getServerId());
			Request apiRequest = getCachedApiRequest();
			UserInfoResponse userInfoResponse = apiRequest.getUserInfo(accessTokenResponse);
			
			if (userInfoResponse.isOK()) {
				JSONObject userObject = userInfoResponse.getContentAsJSONObject();
				
				String subject = (String) userObject.get("id");
				String userName = (String) userObject.get("username");
				String email = StringUtils.trimToNull((String) userObject.get("email"));
				Boolean verified = (Boolean) userObject.get("verified");
				if (verified != null && !verified)
					email = null;
				
				if (bCheckGuilds) {
					UserGuildsResponse guildsResponse = apiRequest.getUserGuilds(accessTokenResponse);
					
					if (guildsResponse.isOK()) {
						JSONObject serverInfo = guildsResponse.getServerInfo(getServerId());
						
						if (serverInfo == null) {
							throw new AuthenticationException(_T("You are not member of discord server"));
						}
					} else {
						throw new AuthenticationException(_T("Unable to get guilds info"));
					}
				}

				return new SsoAuthenticated(subject, userName, email, null, null, null);
			} else {
				String errorMessage = userInfoResponse.getMessage();
				if (errorMessage != null) {
					throw new AuthenticationException(errorMessage);
				}
				
				throw new AuthenticationException(userInfoResponse.getContent());
			}
		} catch (IOException | JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getScopes() {
		String scopes = "identify email";
		if (!StringUtils.isEmpty(this.serverId)) {
			scopes += " guilds"; 
		}
		
		return scopes;
	}
	
	private Request getCachedApiRequest() {
		Request metadata = (Request) Session.get().getAttribute(SESSION_ATTR_API_REQUEST);
		
		if (metadata == null) {
			throw new AuthenticationException(_T("Unsolicited discord api response"));
		}
		
		return metadata;
	}
}
