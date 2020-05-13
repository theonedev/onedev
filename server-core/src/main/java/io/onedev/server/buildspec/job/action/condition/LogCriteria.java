package io.onedev.server.buildspec.job.action.condition;

import java.util.regex.Pattern;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.LogManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class LogCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String value;
	
	public LogCriteria(String value) {
		this.value = value;
	}
	
	@Override
	public boolean matches(Build build) {
		Pattern pattern = Pattern.compile(value);
		return OneDev.getInstance(LogManager.class).matches(build, pattern);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_LOG) + " " 
				+ ActionCondition.getRuleName(ActionConditionLexer.Contains) + " "
				+ quote(value);
	}
	
}
