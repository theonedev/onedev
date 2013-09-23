package com.pmease.commons.wicket.editable.password;

import java.io.Serializable;
import java.util.Map;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public class ConfirmativePasswordPropertyEditContext extends PropertyEditContext {

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
		if (getPropertyValue() != null) {
			return new Label((String) renderParam, "******");
		} else {
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

	@Override
	protected void doValidation() {
		if (password == null)
			error("Please specify the password.");
		else if (confirmedPassword == null)
			error("Please confirm the password.");
		else if (!password.equals(confirmedPassword))
			error("Password and its confirmation should be identical.");
		else
			setPropertyValue(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
		
		if (!hasValidationError(true))
			super.doValidation();
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return null;
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
