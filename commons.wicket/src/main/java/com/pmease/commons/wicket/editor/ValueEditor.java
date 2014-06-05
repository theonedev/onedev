package com.pmease.commons.wicket.editor;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

@SuppressWarnings("serial")
public abstract class ValueEditor<T> extends FormComponentPanel<T> implements ErrorContext {

	public ValueEditor(String id, IModel<T> model) {
		super(id, model);
	}

	public ErrorContext getErrorContext(ValuePath partPath) {
		ErrorContext context = this;
		for (PathSegment segment: partPath.getElements()) {
			ErrorContext segmentContext = context.getErrorContext(segment);
			if (segmentContext != null)
				context = segmentContext;
			else 
				break;
		}
		return context;
	}
	
	public abstract ErrorContext getErrorContext(PathSegment pathSegment);

	protected abstract T convertInputToValue() throws ConversionException;
	
	@Override
	protected void convertInput() {
		try {
			setConvertedInput(convertInputToValue());
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}

	@Override
	public void addError(String errorMessage) {
		error(errorMessage);
	}

	@Override
	public boolean hasErrors() {
		return !isValid();
	}

}
