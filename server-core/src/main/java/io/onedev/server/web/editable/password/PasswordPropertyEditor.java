package io.onedev.server.web.editable.password;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

public class PasswordPropertyEditor extends PropertyEditor<String> {

	private PasswordTextField input;
	
	public PasswordPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new PasswordTextField("input", Model.of(getModelObject()));
		input.setRequired(false);
		input.setResetPassword(false);
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
		add(input);

		Password password = getDescriptor().getPropertyGetter().getAnnotation(Password.class);
		String autoComplete = password.autoComplete();
		if (StringUtils.isNotBlank(autoComplete))
			input.add(AttributeModifier.replace("autocomplete", autoComplete));
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
