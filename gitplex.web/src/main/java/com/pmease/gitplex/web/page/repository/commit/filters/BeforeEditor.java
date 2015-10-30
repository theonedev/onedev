package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.model.Model;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.pmease.gitplex.web.Constants;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextFieldConfig;

@SuppressWarnings("serial")
public class BeforeEditor extends FilterEditor {

	private DateTextField input;
	
	public BeforeEditor(String id, CommitFilter filter, boolean focus) {
		super(id, filter, focus);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Date date;
		List<String> values = getModelObject();
		if (!values.isEmpty())
			date = Constants.DATE_FORMATTER.parseDateTime(values.get(0)).toDate();
		else
			date = null;

		DateTextFieldConfig config = new DateTextFieldConfig();
		config.highlightToday(true);
		config.autoClose(true);
		config.clearButton(true);
		config.forceParse(true);
		config.withFormat(Constants.DATE_FORMAT);
		input = new DateTextField("input", Model.of(date), config);
		
		add(input);
	}

	@Override
	protected void convertInput() {
		Date date = input.getConvertedInput();
		if (date != null) 
			setConvertedInput(Lists.newArrayList(Constants.DATE_FORMATTER.print(new DateTime(date))));
		else
			setConvertedInput(new ArrayList<String>());
	}

	@Override
	protected String getFocusScript() {
		return String.format("$('#%s').focus();", input.getMarkupId());
	}

}
