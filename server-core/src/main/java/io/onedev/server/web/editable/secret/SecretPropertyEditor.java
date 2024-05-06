package io.onedev.server.web.editable.secret;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.Secret;

@SuppressWarnings("serial")
public class SecretPropertyEditor extends PropertyEditor<String> {

	private PasswordTextField input;
	
	public SecretPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new PasswordTextField("input", Model.of(getModelObject()));
		input.setRequired(false);
		input.setResetPassword(false);
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		add(input);

		Secret password = getDescriptor().getPropertyGetter().getAnnotation(Secret.class);
		String autoComplete = password.autoComplete();
		if (StringUtils.isNotBlank(autoComplete))
			input.add(AttributeAppender.append("autocomplete", autoComplete));
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
