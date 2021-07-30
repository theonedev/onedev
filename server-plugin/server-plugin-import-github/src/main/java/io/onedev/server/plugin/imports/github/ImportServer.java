package io.onedev.server.plugin.imports.github;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.wicket.MetaDataKey;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	static final MetaDataKey<ImportServer> META_DATA_KEY = new MetaDataKey<ImportServer>() {

		private static final long serialVersionUID = 1L;
		
	};
	
	static final String PROP_API_URL = "apiUrl";
	
	static final String PROP_ACCESS_TOKEN = "accessToken";
	
	private String apiUrl = "https://api.github.com";
	
	private String accessToken;

	@Editable(order=10, name="GitHub API URL", description="Specify GitHub API url, for instance <tt>https://api.github.com</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=100, name="GitHub Personal Access Token", description="GitHub personal access token should be generated with "
			+ "scope <b>repo</b> and <b>read:org</b>")
	@Password
	@NotEmpty
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic("git", getAccessToken()));
		try {
			String apiEndpoint = getApiEndpoint("/user");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a GitHub api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_ACCESS_TOKEN).addConstraintViolation();
					return false;
				} else {
					String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
					if (errorMessage != null) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate(errorMessage)
								.addPropertyNode(PROP_API_URL).addConstraintViolation();
						return false;
					} 
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String errorMessage = "Error connecting api service";
			if (e.getMessage() != null)
				errorMessage += ": " + e.getMessage();
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode(PROP_API_URL).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
	Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic("git", getAccessToken()));
		return client;
	}
	
}
