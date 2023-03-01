package io.onedev.server.web.util.editablebean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.model.User;
import io.onedev.server.annotation.Editable;

@Editable
public class NewUserBean extends User {

	private static final long serialVersionUID = 1L;

	public static final String PROP_EMAIL_ADDRESS = "emailAddress";
	
	private String emailAddress;

	@Editable(order=1000)
	@NotEmpty
	@Email
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
}
