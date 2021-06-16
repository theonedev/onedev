package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
@ClassValidating
public class YouTrackImportSource implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final String PROP_API_URL = "apiUrl";
	
	private static final String PROP_USER_NAME = "userName";
	
	private static final String PROP_PASSWORD = "password";
	
	private String apiUrl;
	
	private String userName;
	
	private String password;
	
	private boolean prepopulateImportOptions = true;

	@Editable(order=10, name="YouTrack API URL", description="Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	@Editable(order=200, name="YouTrack User Name", description="Specify YouTrack user name. This account should have permission to:"
			+ "<ul>"
			+ "<li>Read full information and issues of the projects you want to import"
			+ "<li>Read issue tags"
			+ "<li>Read user basic information"
			+ "</ul>")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=300, name="YouTrack Password or Access Token", description="Specify YouTrack password or access token for above user")
	@Password
	@NotEmpty
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Editable(order=400, description="If checked, import options will be pre-populated based on all accessible "
			+ "projects and its settings. In case there are too many projects and settings, you may want to "
			+ "uncheck this and provide import option manually")
	public boolean isPrepopulateImportOptions() {
		return prepopulateImportOptions;
	}

	public void setPrepopulateImportOptions(boolean prepopulateImportOptions) {
		this.prepopulateImportOptions = prepopulateImportOptions;
	}

	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
		try {
			WebTarget target = client.target(getApiEndpoint("/users/me?fields=guest"));
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				String errorMessage = YouTrackImporter.checkStatus(response);
				if (errorMessage != null) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else {
					JsonNode userNode = response.readEntity(JsonNode.class);
					if (userNode.get("guest").asBoolean()) {
						context.disableDefaultConstraintViolation();
						errorMessage = "Authentication failed";
						context.buildConstraintViolationWithTemplate(errorMessage)
								.addPropertyNode(PROP_USER_NAME).addConstraintViolation();
						context.buildConstraintViolationWithTemplate(errorMessage)
								.addPropertyNode(PROP_PASSWORD).addConstraintViolation();
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
	
}
