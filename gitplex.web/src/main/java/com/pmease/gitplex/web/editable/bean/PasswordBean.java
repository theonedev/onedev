package com.pmease.gitplex.web.editable.bean;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Password;

@Editable
public class PasswordBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String password;
	
	@Editable
	@Password(confirmative=true)
	@NotEmpty
	@Length(min=5)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
