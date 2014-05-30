package com.pmease.commons.wicket.editable.password;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class ConfirmativePasswordPropertyEditContext extends PropertyEditContext {

	private String password;
	
	private String confirmedPassword;
	
	public ConfirmativePasswordPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new ConfirmativePasswordPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		return new Label(componentId, "******");
	}

	@Override
	public void updateBean() {
		if (password != null) {
			if (confirmedPassword == null)
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
