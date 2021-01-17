package io.onedev.server.web.component.user.passwordedit;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.CurrentPassword;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class PasswordEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oldPassword;
	
	private String newPassword;

	@Editable(order=100)
	@CurrentPassword
	@Password
	@NotEmpty
	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@Editable(order=200)
	@Password(needConfirm=true)
	@NotEmpty
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
