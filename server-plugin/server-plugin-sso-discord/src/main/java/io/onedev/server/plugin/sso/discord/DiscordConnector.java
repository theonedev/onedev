package io.onedev.server.plugin.sso.discord;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import javax.validation.constraints.NotEmpty;
import org.json.JSONObject;
import org.json.JSONException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.plugin.sso.discord.oauth2.AccessTokenResponse;
import io.onedev.server.plugin.sso.discord.oauth2.Request;
import io.onedev.server.plugin.sso.discord.oauth2.UserGuildsResponse;
import io.onedev.server.plugin.sso.discord.oauth2.UserInfoResponse;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.page.admin.ssosetting.SsoProcessPage;

@Editable(name="Discord", order=150, description="Single sign on via discord.com")
public class DiscordConnector extends SsoConnector {
	
	private static final long serialVersionUID = 1L;
	private static final String SESSION_ATTR_API_REQUEST = "DiscordApiRequest";
	
	private String clientId;
	private String clientSecret;
	private String serverId;
	
	public DiscordConnector() {
		setName("Discord");
	}
	
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
		ResourceReference logo = new PackageResourceReference(DiscordConnector.class, "discord.png");
		return RequestCycle.get().urlFor(logo, new PageParameters()).toString();
	}

	@Override
	public URI getCallbackUri() {
		String serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		try {
			return new URI(serverUrl + "/" + SsoProcessPage.MOUNT_PATH + "/" 
					+ SsoProcessPage.STAGE_CALLBACK + "/" + getName());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SsoAuthenticated processLoginResponse() {
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
				throw new AuthenticationException("Invalid state response");
			}
		} else {
			throw new AuthenticationException(codeResponse.getError());
		}
	}

	@Override
	public void initiateLogin() {
		Request apiRequest = new Request(getClientId(), getScopes(), getCallbackUri().toString());
		Session.get().setAttribute(SESSION_ATTR_API_REQUEST, apiRequest);
		
		String authURI = apiRequest.getAuthorizationURI();
		throw new RedirectToUrlException(authURI);
	}

	@Override
	public boolean isManagingMemberships() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private SsoAuthenticated processTokenResponse(AccessTokenResponse accessTokenResponse) {
		try {
			final boolean bCheckGuilds = !StringUtils.isEmpty(getServerId());
			Request apiRequest = getCachedApiRequest();
			UserInfoResponse userInfoResponse = apiRequest.getUserInfo(accessTokenResponse);
			
			if (userInfoResponse.isOK()) {
				JSONObject userObject = userInfoResponse.getContentAsJSONObject();
				
				String userName = (String) userObject.get("username");
				String email = (String) userObject.get("email");
				
				if (StringUtils.isBlank(email))
					throw new AuthenticationException("A public email is required");
				
				if (bCheckGuilds) {
					UserGuildsResponse guildsResponse = apiRequest.getUserGuilds(accessTokenResponse);
					
					if (guildsResponse.isOK()) {
						JSONObject serverInfo = guildsResponse.getServerInfo(getServerId());
						
						if (serverInfo == null) {
							throw new AuthenticationException("You are not member of discord server");
						}
					} else {
						throw new AuthenticationException("Unable to get guilds info");
					}
				}
				
				return new SsoAuthenticated(userName, email, null, null, null, this);
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
			throw new AuthenticationException("Unsolicited discord api response");
		}
		
		return metadata;
	}
}
