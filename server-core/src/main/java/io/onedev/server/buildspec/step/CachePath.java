package io.onedev.server.buildspec.step;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CachePathFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;

@Editable
public class CachePath implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean relativeToHomeIfNotAbsolute = false;

	private String pathValue;

	@Editable(order=100, description = """
		If true, the path is considered to be relative to user home directory if not absolute; 
		otherwise, it is considered to be relative to job workdir""")
	public boolean isRelativeToHomeIfNotAbsolute() {
		return relativeToHomeIfNotAbsolute;
	}

	public void setRelativeToHomeIfNotAbsolute(boolean relativeToHomeIfNotAbsolute) {
		this.relativeToHomeIfNotAbsolute = relativeToHomeIfNotAbsolute;
	}

	@Editable(order=200, name="Path")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getPathValue() {
		return pathValue;
	}

	public void setPathValue(String pathValue) {
		this.pathValue = pathValue;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	public CachePathFacade toFacade() {
		return new CachePathFacade(relativeToHomeIfNotAbsolute, pathValue);
	}

	public static CachePath of(boolean relativeToHomeIfNotAbsolute, String pathValue) {
		var cachePath = new CachePath();
		cachePath.setRelativeToHomeIfNotAbsolute(relativeToHomeIfNotAbsolute);
		cachePath.setPathValue(pathValue);
		return cachePath;
	}

}
