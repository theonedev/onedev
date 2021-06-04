package io.onedev.server.model.support.administration.sso;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
@ClassValidating
public abstract class SsoConnector implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String defaultGroup;

	@Editable(order=100, description="Name of the provider will be displayed on login button")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract String getButtonImageUrl();

	@Editable(order=20000, description="Optionally add newly authenticated user to specified "
			+ "group if membership information is not retrieved")
	@GroupChoice
	@NameOfEmptyValue("No default group")
	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultGroup))
			defaultGroup = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultGroup))
			usage.add("default group");
		return usage;
	}
	
	public Component renderAdditionalInfo(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}
	
	/**
	 * Authenticate specified user name and password against the authentication system 
	 * represented by this provider.
	 * 
	 * @param userName 
	 * 			user name to check
	 * @param password 
	 * 			password to check. If this parameter is null, this method should return 
	 * 			information about the user without checking the password
	 * @return 
	 * 			authentication result if successful
	 */
	public abstract SsoAuthenticated processLoginResponse();
	
	public abstract void initiateLogin();

	public abstract boolean isManagingMemberships();

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (getName() != null) {
			for (SsoConnectorContribution contribution: OneDev.getExtensions(SsoConnectorContribution.class)) {
				for (SsoConnector ssoConnector: contribution.getSsoConnectors()) {
					if (getName().equalsIgnoreCase(ssoConnector.getName())) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate("This name is reserved")
								.addPropertyNode("name").addConstraintViolation();
						return false;
					}
				}
			}
		} 
		return true;
	}
	
}
