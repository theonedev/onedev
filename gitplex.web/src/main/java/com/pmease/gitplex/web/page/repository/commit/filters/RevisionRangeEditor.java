package com.pmease.gitplex.web.page.repository.commit.filters;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.ConversionException;

@SuppressWarnings("serial")
public class RevisionRangeEditor extends FilterEditor<String> {

	private TextField<String> input;
	
	public RevisionRangeEditor(String id, CommitFilter filter) {
		super(id, filter);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextField<String>("revision range"));
	}

	@Override
	public void onEdit(AjaxRequestTarget target) {
		
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}
	
}
