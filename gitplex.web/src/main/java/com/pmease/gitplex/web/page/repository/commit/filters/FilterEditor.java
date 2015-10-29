package com.pmease.gitplex.web.page.repository.commit.filters;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.util.convert.ConversionException;

@SuppressWarnings("serial")
public abstract class FilterEditor<T> extends FormComponentPanel<T> {

	protected final CommitFilter filter; 
	
	public FilterEditor(String id, CommitFilter filter) {
		super(id);
		
		this.filter = filter;
	}

	@Override
	protected void convertInput() {
		try {
			setConvertedInput(convertInputToValue());
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}
	
	protected abstract T convertInputToValue() throws ConversionException;
	
	public abstract void onEdit(AjaxRequestTarget target);
}
