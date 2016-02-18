package com.pmease.gitplex.web.editable.path;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.util.editable.PathChoice;
import com.pmease.gitplex.web.component.pathchoice.PathSingleChoice;

@SuppressWarnings("serial")
public class PathSingleChoiceEditor extends PropertyEditor<String> {

	private PathSingleChoice input;
	
	public PathSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PathChoice pathChoice = getPropertyDescriptor().getPropertyGetter().getAnnotation(PathChoice.class);
		add(input = new PathSingleChoice("input", getModel(), pathChoice.value()));
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