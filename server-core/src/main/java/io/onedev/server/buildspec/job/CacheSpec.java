package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.util.validation.annotation.RegEx;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class CacheSpec implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private String path;

	@Editable(order=100, description="Specify key of the cache. Caches with same key can be reused by different jobs")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	@RegEx(pattern="[a-zA-Z0-9\\-_\\.]+", message="Can only contain alphanumeric, dash, dot and underscore")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Editable(order=200, description="Specify path to cache. Non-absolute path is considered to be relative to job workspace. "
			+ "Specify \".\" (without quote) to cache workspace itself. "
			+ "<span class='text-warning'>Absolute path is not allowed if the job is executed by a shell/batch executor</span>")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestPaths")
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestPaths(String matchWith) {
		Map<String, String> candidates = new LinkedHashMap<>();
		candidates.put(KubernetesHelper.HOME_PREFIX, "Path under user home");
		return SuggestionUtils.suggest(candidates, matchWith); 
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}

}
