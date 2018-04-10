package io.onedev.server.web.editable.password;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.util.editable.annotation.Password;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
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
		input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
		add(input);

		Password password = getPropertyDescriptor().getPropertyGetter().getAnnotation(Password.class);
		String autoComplete = password.autoComplete();
		if (StringUtils.isNotBlank(autoComplete))
			input.add(AttributeAppender.append("autocomplete", autoComplete));
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
