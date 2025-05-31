package io.onedev.server.web.editable.password;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class ConfirmativePasswordPropertyEditor extends PropertyEditor<String> {
	
	private PasswordTextField input;
	
	private PasswordTextField inputAgain;
	
	public ConfirmativePasswordPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new PasswordTextField("input", Model.of(getModelObject()));
		input.setResetPassword(true);
		input.setRequired(false);
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
		add(input);
		
		inputAgain = new PasswordTextField("inputAgain", Model.of(getModelObject()));
		inputAgain.setResetPassword(true);
		inputAgain.setRequired(false);
		inputAgain.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
		add(inputAgain);
		
		Password password = getDescriptor().getPropertyGetter().getAnnotation(Password.class);
		String autoComplete = password.autoComplete();
		if (StringUtils.isNotBlank(autoComplete)) {
			input.add(AttributeAppender.replace("autocomplete", autoComplete));
			inputAgain.add(AttributeAppender.replace("autocomplete", autoComplete));
		}
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		inputAgain.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		if (input.getConvertedInput() != null) {
			if (inputAgain.getConvertedInput() == null)
				throw new ConversionException(_T("Please confirm the password."));
			else if (!input.getConvertedInput().equals(inputAgain.getConvertedInput()))
				throw new ConversionException(_T("Password and its confirmation should be identical."));
			else
				return input.getConvertedInput();
		} else if (inputAgain.getConvertedInput() != null) {
			throw new ConversionException(_T("Password and its confirmation should be identical."));
		} else {
			return input.getConvertedInput();
		}
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
