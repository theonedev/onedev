package io.onedev.server.buildspec.job;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable
public class SubmoduleCredential extends Authentication {

	private static final long serialVersionUID = 1L;

	private String url;
	
	@Editable(order=100, description="Specify submodule url. Note: Input '@' to start inserting variable. "
			+ "Use \\ to escape normal occurrences of @ or \\")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

}