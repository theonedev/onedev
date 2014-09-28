package com.pmease.commons.wicket.editable.string;

import java.lang.reflect.Method;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.annotation.Markdown;
import com.pmease.commons.editable.annotation.Multiline;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.commons.wicket.component.markdown.MarkdownInput;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class StringPropertyEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public StringPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Method getter = getPropertyDescriptor().getPropertyGetter();
		if (getter.getAnnotation(Markdown.class) != null) {
			add(input = new MarkdownInput("content", Model.of(getModelObject())));
		} else if (getter.getAnnotation(Multiline.class) != null) {
			Fragment fragment = new Fragment("content", "multiLineFrag", this);
			fragment.add(input = new TextArea<String>("input", Model.of(getModelObject())));
			input.setType(getPropertyDescriptor().getPropertyClass());
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "singleLineFrag", this);
			fragment.add(input = new TextField<String>("input", Model.of(getModelObject())));
			input.setType(getPropertyDescriptor().getPropertyClass());
			add(fragment);
		}
		Method propertyGetter = getPropertyDescriptor().getPropertyGetter();
		if (propertyGetter.getAnnotation(OmitName.class) != null)
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getName(propertyGetter)));

		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasError(true))
					return " has-error";
				else
					return "";
			}
			
		}));
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
