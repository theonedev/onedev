package io.onedev.server.plugin.sso.openid;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authc.AuthenticationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Editable(name="Microsoft Entra ID", order=125)
public class EntraIdConnector extends OpenIdConnector {

	private static final long serialVersionUID = 1L;

	private String tenantId;
	
	private boolean retrieveGroups;
	
	@Editable(order=1000, name="Application (client) ID", description="Specify application (client) ID of the app registered in Entra ID")
	@NotEmpty
	public String getClientId() {
		return super.getClientId();
	}

	public void setClientId(String clientId) {
		super.setClientId(clientId);
	}

	@Editable(order=1050, name="Directory (tenant) ID", description="Specify directory (tenant) ID of the app registered in Entra ID")
	@NotEmpty
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Editable(order=1100, description="Specify client secret of the app registered in Entra ID")
	@Password
	@NotEmpty
	public String getClientSecret() {
		return super.getClientSecret();
	}

	@Editable(order=1200, description = "Whether or not to retrieve groups of login user. " +
			"Make sure to add groups claim via token configuration of the app registered " +
			"in Entra ID if this option is enabled. The groups claim should return group " +
			"id (the default option) via various token types in this case")
	public boolean isRetrieveGroups() {
		return retrieveGroups;
	}

	public void setRetrieveGroups(boolean retrieveGroups) {
		this.retrieveGroups = retrieveGroups;
	}

	public void setClientSecret(String clientSecret) {
		super.setClientSecret(clientSecret);
	}

	@Override
	public String getConfigurationDiscoveryUrl() {
		return "https://login.microsoftonline.com/" + getTenantId() + "/v2.0/.well-known/openid-configuration";
	}

	@Override
	public String getRequestScopes() {
		return "openid email profile";
	}
	
	@Override
	public String getGroupsClaim() {
		if (isRetrieveGroups())
			return "groups";
		else
			return null;
	}
	
	@Override
	public String getButtonImageUrl() {
		return "/wicket/resource/" + EntraIdConnector.class.getName() + "/entraid.png";
	}

	@Override
	protected List<String> convertGroups(BearerAccessToken accessToken, List<String> groups) {
		try {
			var httpRequest = new HTTPRequest(HTTPRequest.Method.GET, 
					URI.create(String.format("https://graph.microsoft.com/v1.0/%s/groups", getTenantId())));
			httpRequest.setAccept(MediaType.APPLICATION_JSON);
			httpRequest.setAuthorization(accessToken.toAuthorizationHeader());
			var httpResponse = httpRequest.send();
			if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
				var groupNames = new HashMap<String, String>();
				JSONObject json = httpResponse.getBodyAsJSONObject();
				var jsonArray = (JSONArray) json.get("value");
				for (var jsonArrayElement: jsonArray) {
					var groupInfo = (JSONObject) jsonArrayElement;
					var groupId = groupInfo.getAsString("id");
					var groupName = groupInfo.getAsString("displayName");
					groupNames.put(groupId, groupName);
				}
				var convertedGroups = new ArrayList<String>();
				for (var group: groups) {
					var convertedGroup = groupNames.get(group);
					if (convertedGroup != null)
						convertedGroups.add(convertedGroup);
					else 
						convertedGroups.add(group);
				}
				return convertedGroups;
			} else {
				List<String> details = new ArrayList<>();
				var body = httpResponse.getBody();
				if (StringUtils.isNotBlank(body)) {
					try {
						var errorNode = OneDev.getInstance(ObjectMapper.class).readTree(body).get("error");
						details.add("code: " + errorNode.get("code").asText());
						details.add("message: " + errorNode.get("message").asText());
					} catch (Exception e) {
						details.add("response: " + body);
					}
				}
				details.add("http status code: " + httpResponse.getStatusCode());
				throw new AuthenticationException(StringUtils.join(details, ", "));
			}
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
}
