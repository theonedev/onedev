package io.onedev.server.web.editable.string;

import java.lang.reflect.Method;

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
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.Multiline;

@SuppressWarnings("serial")
public class StringPropertyEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	private InputAssistBehavior inputAssist;
	
	public StringPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Method getter = getDescriptor().getPropertyGetter();
		var multiline = getter.getAnnotation(Multiline.class);
		if (multiline != null) {
			Fragment fragment = new Fragment("content", "multiLineFrag", this);
			fragment.add(input = new TextArea<>("input", Model.of(getModelObject())) {

				@Override
				protected boolean shouldTrimInput() {
					return false;
				}

			});
			input.setType(getDescriptor().getPropertyClass());
			if (multiline.monospace()) 
				input.add(AttributeAppender.append("class", "text-monospace"));
			if (multiline.maxHeight().length() != 0)
				input.add(AttributeAppender.append("style", "max-height: " + multiline.maxHeight()));
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "singleLineFrag", this);
			fragment.add(input = new TextField<String>("input", Model.of(getModelObject())));
			input.setType(getDescriptor().getPropertyClass());
			add(fragment);
		}
		input.setLabel(Model.of(getDescriptor().getDisplayName()));		
		
		if (inputAssist != null) {
			input.add(inputAssist);
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

		input.add(newPlaceholderModifier());
		
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	public StringPropertyEditor setInputAssist(InputAssistBehavior inputAssist) {
		this.inputAssist = inputAssist;
		return this;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}
