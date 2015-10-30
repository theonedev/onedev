package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.component.datetime.DatetimePicker;
import com.pmease.gitplex.web.Constants;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig;

@SuppressWarnings("serial")
public class DateEditor extends FilterEditor {

	private DatetimePicker input;
	
	private final String placeholder;
	
	public DateEditor(String id, CommitFilter filter, boolean focus, String placeholder) {
		super(id, filter, focus);
		
		this.placeholder = placeholder;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Date date;
		List<String> values = getModelObject();
		if (!values.isEmpty())
			date = Constants.DATETIME_FORMATTER.parseDateTime(values.get(0)).toDate();
		else
			date = null;

		DatetimePickerConfig config = new DatetimePickerConfig();
		config.setShowToday(true);
		config.withFormat(Constants.DATETIME_FORMAT);
		add(input = new DatetimePicker("input", Model.of(date), config));
		input.add(AttributeAppender.append("placeholder", placeholder));
	}

	@Override
	protected void convertInput() {
		Date date = input.getConvertedInput();
		if (date != null) 
			setConvertedInput(Lists.newArrayList(Constants.DATETIME_FORMATTER.print(new DateTime(date))));
		else
			setConvertedInput(new ArrayList<String>());
	}

	@Override
	protected String getFocusScript() {
		return String.format("$('#%s').focus();", input.getMarkupId());
	}

}
