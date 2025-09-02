package io.onedev.server.plugin.sso.openid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import net.minidev.json.JSONObject;

@Editable(name="GitHub", order=100, description="Refer to this <a href='https://docs.onedev.io/tutorials/security/sso-with-github' target='_blank'>tutorial</a> for an example setup")
public class GitHubConnector extends OpenIdConnector {

	private static final long serialVersionUID = 1L;

	@Override
	public String getConfigurationDiscoveryUrl() {
		return super.getConfigurationDiscoveryUrl();
	}

	@Override
	protected ProviderMetadata discoverProviderMetadata() {
		return new ProviderMetadata(
				"https://github.com",
				"https://github.com/login/oauth/authorize", 
				"https://github.com/login/oauth/access_token", 
				"https://api.github.com/user");
	}

	@Override
	public String getButtonImageUrl() {
		return "/wicket/resource/" + GitHubConnector.class.getName() + "/octocat.png";
	}

	@Override
	public String getRequestScopes() {
		return "openid profile user:email";
	}

	@Override
	public String getGroupsClaim() {
		return super.getGroupsClaim();
	}

	@Override
	protected SsoAuthenticated processTokenResponse(OIDCTokenResponse tokenResponse) {
		BearerAccessToken accessToken = tokenResponse.getTokens().getBearerAccessToken();

		try {
			UserInfoRequest userInfoRequest = new UserInfoRequest(
					new URI(getCachedProviderMetadata().getUserInfoEndpoint()), accessToken);
			HTTPResponse httpResponse = userInfoRequest.toHTTPRequest().send();

			if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
				var jsonObject = httpResponse.getBodyAsJSONObject();
				var subject = jsonObject.getAsString("id");
				var userName = jsonObject.getAsString("login");
				var fullName = jsonObject.getAsString("name");
				var email = StringUtils.trimToNull(jsonObject.getAsString("email"));
				if (email == null) {
					userInfoRequest = new UserInfoRequest(
							new URI("https://api.github.com/user/emails"), accessToken);
					httpResponse = userInfoRequest.toHTTPRequest().send();
					if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
						for (var element: httpResponse.getBodyAsJSONArray()) {
							JSONObject emailObject = (JSONObject) element;
							if (emailObject.getAsString("verified").equals("true") 
									&& emailObject.getAsString("primary").equals("true")) {
								email = StringUtils.trimToNull(emailObject.getAsString("email"));
								break;
							}
						}
					} else {
						throw buildException(UserInfoErrorResponse.parse(httpResponse).getErrorObject());
					}
				}
				return new SsoAuthenticated(subject, userName, email, fullName, null, null);
			} else {
				throw buildException(UserInfoErrorResponse.parse(httpResponse).getErrorObject());
			}
		} catch (SerializeException | ParseException | URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
