package com.pmease.commons.wicket.editable.password;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class ConfirmativePasswordPropertyEditContext extends PropertyEditContext {

	private static final int PASSWORD_MIN_LEN = 6;
	
	private String password;
	
	private String confirmedPassword;
	
	public ConfirmativePasswordPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new ConfirmativePasswordPropertyEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		return new Label((String) renderParam, "******");
	}

	@Override
	public void updateBean() {
		if (password != null) {
			if (password.length() < PASSWORD_MIN_LEN)
				addValidationError("Password should take at least " + PASSWORD_MIN_LEN + " characters.");
			else if (confirmedPassword == null)
				addValidationError("Please confirm the password.");
			else if (!password.equals(confirmedPassword))
				addValidationError("Password and its confirmation should be identical.");
		} 
		setPropertyValue(password);
		
		super.updateBean();
	}

	public String getPassword() {
		return password;
	}

	public String getConfirmedPassword() {
		return confirmedPassword;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConfirmedPassword(String confirmedPassword) {
		this.confirmedPassword = confirmedPassword;
	}

}
