package io.onedev.server.buildspec.job.retrycondition;

import java.util.regex.Pattern;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class ErrorMessageCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String value;
	
	public ErrorMessageCriteria(String value) {
		this.value = value;
	}

	@Override
	public boolean matches(Build build) {
		return Pattern.compile(value).matcher(build.getErrorMessage()).find();
	}

	@Override
	public String asString() {
		return quote(BuildQueryConstants.FIELD_ERROR_MESSAGE) + " " 
				+ RetryCondition.getRuleName(RetryConditionLexer.Contains) + " "
				+ quote(value);
	}
	
}
