package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.util.StringUtils;

@SuppressWarnings("serial")
public class RevisionEditor extends FilterEditor {

	private TextField<String> input;
	
	public RevisionEditor(String id, CommitFilter filter, boolean focus) {
		super(id, filter, focus);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String value = StringUtils.join(getModelObject(), " ");
		add(input = new TextField<String>("revisions", Model.of(value)));
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		String value = input.getConvertedInput();
		if (value != null) 
			return StringUtils.splitAndTrim(value, " ,\t");
		else
			return new ArrayList<>();
	}

	@Override
	protected String getFocusScript() {
		return String.format("$('#%s').focus();", input.getMarkupId());
	}
	
}
