package com.pmease.gitplex.web.page.account.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Password;

@Editable
public class PasswordEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oldPassword;
	
	private String newPassword;

	@Editable(order=100)
	@OldPassword
	@Password
	@NotEmpty
	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@Editable(order=200)
	@Password(confirmative=true)
	@NotEmpty
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
