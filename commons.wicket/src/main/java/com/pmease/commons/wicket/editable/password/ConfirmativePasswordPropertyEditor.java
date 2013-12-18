package com.pmease.commons.wicket.editable.password;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.editable.EditableHeaderItem;

@SuppressWarnings("serial")
public class ConfirmativePasswordPropertyEditor extends Panel {
	
	private final ConfirmativePasswordPropertyEditContext editContext;

	public ConfirmativePasswordPropertyEditor(String id, ConfirmativePasswordPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PasswordTextField("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return editContext.getPassword();
			}

			@Override
			public void setObject(String object) {
				editContext.setPassword(object);
			}
			
		}).setResetPassword(true).setRequired(false));
		
		add(new PasswordTextField("inputAgain", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return editContext.getConfirmedPassword();
			}

			@Override
			public void setObject(String object) {
				editContext.setConfirmedPassword(object);
			}
			
		}).setResetPassword(true).setRequired(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(EditableHeaderItem.get());
	}

}
