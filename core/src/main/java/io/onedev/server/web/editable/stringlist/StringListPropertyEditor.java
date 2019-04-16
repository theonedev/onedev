package io.onedev.server.web.editable.stringlist;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class StringListPropertyEditor extends PropertyEditor<List<String>> {
	
	private TextArea<String> input;
	
	public StringListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		String content;
		if (getModelObject() != null)
			content = StringUtils.join(getModelObject(), "\n");
		else
			content = "";
    	input = new TextArea<String>("input", Model.of(content));
    	
    	InputAssistBehavior inputAssistBehavior = getInputAssistBehavior();
    	if (inputAssistBehavior != null)
    		input.add(inputAssistBehavior);
    	
		input.setLabel(Model.of(getDescriptor().getDisplayName(this)));
        
        add(input);
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
		Method getter = getDescriptor().getPropertyGetter();
		if (getter.getAnnotation(OmitName.class) != null) 
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getDisplayName(getter)));
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> value = new ArrayList<>();
		if (input.getConvertedInput() != null)
			value.addAll(Splitter.on("\n").trimResults().omitEmptyStrings().splitToList(input.getConvertedInput()));
		return value;
	}

	@Nullable
	protected InputAssistBehavior getInputAssistBehavior() {
		return null;
	}
	
}
