package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable
@ClassValidating
public class CacheSpec implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private String path;

	@Editable(order=100, description="Specify key of the cache. Caches with same key can be reused by different projects/jobs. "
			+ "Embed project/job variable to prevent cross project/job reuse")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	@RegEx(pattern ="[a-zA-Z0-9\\-_\\.]+", message="Can only contain alphanumeric, dash, dot and underscore")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getNormalizedKey() {
		return getKey().replaceAll("[^a-zA-Z0-9\\-_\\.]", "-");
	}
	
	@Editable(order=200, description="Specify path to cache. Non-absolute path is considered to be relative to job workspace. "
			+ "Please note that shell executor only allows non-absolute path here")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (PathUtils.isCurrent(path)) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid path").addPropertyNode("path").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
