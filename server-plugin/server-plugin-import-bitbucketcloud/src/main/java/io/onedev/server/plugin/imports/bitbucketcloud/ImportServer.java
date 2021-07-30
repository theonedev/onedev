package io.onedev.server.plugin.imports.bitbucketcloud;

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
	
	private String userName;
	
	private String appPassword;

	@Editable(order=10, name="Bitbucket Login Name")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=100, name="Bitbucket App Password", description="Bitbucket app password should be generated with "
			+ "permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>")
	@Password
	@NotEmpty
	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}
	
	public String getApiEndpoint(String apiPath) {
		return "https://api.bitbucket.org/2.0/" + StringUtils.stripStart(apiPath, "/");
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getUserName(), getAppPassword()));
		try {
			String apiEndpoint = getApiEndpoint("/user");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
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
		client.register(HttpAuthenticationFeature.basic(getUserName(), getAppPassword()));
		return client;
	}
	
}
