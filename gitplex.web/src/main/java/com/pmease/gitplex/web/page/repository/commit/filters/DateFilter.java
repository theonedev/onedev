package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.Date;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.web.Constants;

public abstract class DateFilter extends CommitFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public FilterEditor newEditor(String id, boolean focus) {
		String name = getName();
		String placeholder = name.substring(0, 1).toUpperCase() + name.substring(1);		
		return new DateEditor(id, this, focus, placeholder);
	}

	@Override
	public void applyTo(LogCommand logCommand) {
		if (!getValues().isEmpty()) {
			Date date = Constants.DATETIME_FORMATTER.parseDateTime(getValues().get(0)).toDate();
			applyTo(logCommand, date);
		}
	}

	protected abstract void applyTo(LogCommand logCommand, Date date);
}
