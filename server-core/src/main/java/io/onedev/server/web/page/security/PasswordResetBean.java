package io.onedev.server.web.page.security;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Password;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class PasswordResetBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String newPassword;
	
	@Editable(order=200)
	@OmitName
	@Password(checkPolicy=true)
	@NotEmpty
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
