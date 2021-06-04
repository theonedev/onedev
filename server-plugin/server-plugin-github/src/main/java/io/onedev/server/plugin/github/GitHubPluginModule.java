package io.onedev.server.plugin.github;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.Lists;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.model.support.administration.sso.SsoConnectorContribution;
import io.onedev.server.plugin.sso.openid.OpenIdConnector;
import io.onedev.server.plugin.sso.openid.ProviderMetadata;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import net.minidev.json.JSONObject;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitHubPluginModule extends AbstractPluginModule {

	public static final String SSO_CONNECTOR_NAME = "GitHub";
	
	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(AdministrationSettingContribution.class, new AdministrationSettingContribution() {
			
			@Override
			public Class<? extends Serializable> getSettingClass() {
				return GitHubSetting.class;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new DynamicPathPageMapper(GitHubCallbackPage.MOUNT_PATH, GitHubCallbackPage.class));
			}
			
		});				
		
		contribute(SsoConnectorContribution.class, new SsoConnectorContribution() {
			
			@Override
			public Collection<SsoConnector> getSsoConnectors() {
				List<SsoConnector> connectors = Lists.newArrayList();
				SettingManager settingManager = OneDev.getInstance(SettingManager.class);
				GitHubSetting setting = settingManager.getContributedSetting(GitHubSetting.class);
				if (setting != null) {
					connectors.add(new OpenIdConnector() {

						private static final long serialVersionUID = 1L;
	
						@Override
						public String getName() {
							return SSO_CONNECTOR_NAME;
						}
						
						@Override
						public String getClientId() {
							return setting.getClientId();
						}

						@Override
						public String getClientSecret() {
							return setting.getClientSecret();
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
							ResourceReference logo = new PackageResourceReference(GitHubPluginModule.class, "octocat.png");
							return RequestCycle.get().urlFor(logo, new PageParameters()).toString();
						}
						
						@Override
						protected SsoAuthenticated processTokenResponse(OIDCAccessTokenResponse tokenSuccessResponse) {
							BearerAccessToken accessToken = (BearerAccessToken) tokenSuccessResponse.getAccessToken();
	
							try {
								UserInfoRequest userInfoRequest = new UserInfoRequest(
										new URI(getCachedProviderMetadata().getUserInfoEndpoint()), accessToken);
								HTTPResponse httpResponse = userInfoRequest.toHTTPRequest().send();
	
								if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
									JSONObject json = httpResponse.getContentAsJSONObject();
									String userName = (String) json.get("login");
									String email = (String) json.get("email");
									if (StringUtils.isBlank(email))
										throw new AuthenticationException("A public email is required");
									String fullName = (String) json.get("name");
									
									return new SsoAuthenticated(userName, userName, email, fullName, null, null, this);
								} else {
									throw buildException(UserInfoErrorResponse.parse(httpResponse).getErrorObject());
								}
							} catch (SerializeException | ParseException | URISyntaxException | IOException e) {
								throw new RuntimeException(e);
							}
						}
	
						@Override
						public boolean isManagingMemberships() {
							return false;
						}
						
						protected URI getCallbackUri() {
							String serverUrl = settingManager.getSystemSetting().getServerUrl();
							try {
								return new URI(serverUrl + "/" + GitHubCallbackPage.MOUNT_PATH);
							} catch (URISyntaxException e) {
								throw new RuntimeException(e);
							}
						}
						
					});		
				}
				return connectors;
			}
			
		});
		
	}

}
