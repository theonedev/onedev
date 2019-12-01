package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.util.validation.annotation.RegEx;
import io.onedev.server.util.validation.annotation.Path;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable
public class CacheSpec implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private String path;

	@Editable(order=100, description="Specify key of the cache. Caches with same key can be reused by different jobs. "
			+ "Note: Type @ to insert variable, use \\ to escape normal occurrences of @ or \\")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	@RegEx(interpolative=true, pattern="[a-zA-Z0-9\\-_\\.]+", message="Can only contain alphanumeric, dash, dot and underscore")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Editable(order=200, description="Specify path to cache. Non-absolute path is considered to be relative to job workspace. "
			+ "Specify \".\" (without quote) to cache workspace itself. "
			+ "Note: Type @ to insert variable, use \\ to escape normal occurrences of @ or \\")
	@Interpolative(variableSuggester="suggestVariables")
	@Path
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

}
