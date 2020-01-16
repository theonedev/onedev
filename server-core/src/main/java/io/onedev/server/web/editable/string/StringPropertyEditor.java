package io.onedev.server.web.editable.string;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class StringPropertyEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public StringPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Method getter = getDescriptor().getPropertyGetter();
		if (getter.getAnnotation(Multiline.class) != null) {
			Fragment fragment = new Fragment("content", "multiLineFrag", this);
			fragment.add(input = new TextArea<String>("input", Model.of(getModelObject())));
			input.setType(getDescriptor().getPropertyClass());
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "singleLineFrag", this);
			fragment.add(input = new TextField<String>("input", Model.of(getModelObject())));
			input.setType(getDescriptor().getPropertyClass());
			add(fragment);
		}
		input.setLabel(Model.of(getDescriptor().getDisplayName()));		
		
		InputAssistBehavior inputAssistBehavior = getInputAssistBehavior();
		if (inputAssistBehavior != null) {
			input.add(inputAssistBehavior);
			input.add(AttributeAppender.append("spellcheck", "false"));
			input.add(AttributeAppender.append("autocomplete", "off"));
			if (!getDescriptor().isPropertyRequired())
				input.add(AttributeAppender.append("class", "no-autofocus"));
		}
		
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
		if (getter.getAnnotation(OmitName.class) != null) {
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getDisplayName(getter)));
		} else {
			NameOfEmptyValue nameOfEmptyValue = getter.getAnnotation(NameOfEmptyValue.class);
			if (nameOfEmptyValue != null)
				input.add(AttributeModifier.replace("placeholder", nameOfEmptyValue.value()));
		}
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Nullable
	protected InputAssistBehavior getInputAssistBehavior() {
		return null;
	}
	
}
