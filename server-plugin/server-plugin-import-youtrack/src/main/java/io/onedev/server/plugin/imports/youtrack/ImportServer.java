package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.wicket.MetaDataKey;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	static final MetaDataKey<ImportServer> META_DATA_KEY = new MetaDataKey<ImportServer>() {

		private static final long serialVersionUID = 1L;
		
	};
	
	protected static final String PROP_API_URL = "apiUrl";
	
	private String apiUrl;
	
	private String userName;
	
	private String password;
	
	@Editable(order=10, name="YouTrack API URL", description="Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	@Editable(order=200, name="YouTrack Login Name", description="Specify YouTrack login name. This account should have permission to:"
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

	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		try {
			client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
			String apiEndpoint = getApiEndpoint("/users/me?fields=guest");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a YouTrack api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} 				
				String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
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
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode(PROP_API_URL).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
	List<String> getProjectChoices() {
		List<String> choices = new ArrayList<>();
		
		Client client = newClient();
		try {
			TaskLogger logger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					ImportServer.logger.info(message);
				}
				
			};
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=name");
			for (JsonNode projectNode: ImportUtils.list(client, apiEndpoint, logger)) 
				choices.add(projectNode.get("name").asText());
		} finally {
			client.close();
		}
		
		Collections.sort(choices);
		return choices;
	}
	
	Client newClient() {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
		return client;
	}

}
