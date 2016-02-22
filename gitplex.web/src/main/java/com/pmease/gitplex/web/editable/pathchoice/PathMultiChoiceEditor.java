package com.pmease.gitplex.web.editable.pathchoice;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.annotation.PathChoice;
import com.pmease.gitplex.web.component.pathchoice.PathMultiChoice;

@SuppressWarnings("serial")
public class PathMultiChoiceEditor extends PropertyEditor<List<String>> {

	private PathMultiChoice input;
	
	public PathMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PathChoice pathChoice = getPropertyDescriptor().getPropertyGetter().getAnnotation(PathChoice.class);
		add(input = new PathMultiChoice("input", getModel(), pathChoice.value()));
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}
	
}