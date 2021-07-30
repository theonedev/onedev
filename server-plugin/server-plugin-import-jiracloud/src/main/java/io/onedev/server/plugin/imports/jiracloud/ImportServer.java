package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wicket.MetaDataKey;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.Email;
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
	
	private String apiUrl;
	
	private String accountEmail;
	
	private String apiToken;

	@Editable(order=5, description="API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=10)
	@Email
	@NotEmpty
	public String getAccountEmail() {
		return accountEmail;
	}

	public void setAccountEmail(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	@Editable(order=100)
	@Password
	@NotEmpty
	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}
	
	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getAccountEmail(), getApiToken()));
		try {
			String apiEndpoint = getApiEndpoint("/myself");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			builder.header("Accept", MediaType.APPLICATION_JSON);
			try (Response response = builder.get()) {
				if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
					return false;
				} else if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a JIRA api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else {
					String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
					if (errorMessage != null) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
						return false;
					} 
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String errorMessage = "Error connecting api service";
			if (e.getMessage() != null)
				errorMessage += ": " + e.getMessage();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
	Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic(getAccountEmail(), getApiToken()));
		return client;
	}
	
}
