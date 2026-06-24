package io.onedev.server.buildspec.step;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CacheEntryFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Path;
import io.onedev.server.buildspec.BuildSpec;

@Editable
public class CacheEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String path;

	private String excludes;

	@Editable(order=100, description = """
			Specify cache path. Non-absolute path is considered to be relative to 
			<a href='https://docs.onedev.io/concepts#job-workdir' target='_blank'>job working directory</a>. 
			Note that shell related executors runs directly on host machine, and only accept relative paths""")
	@Path
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Editable(order=200, placeholder = "None", description = """
			Optionally specify files or directories relative to cache path to exclude. 
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. 
			Multiple excludes should be separated by space, and single exclude containing space should be quoted""")
	@Interpolative(variableSuggester="suggestVariables")
	public String getExcludes() {
		return excludes;
	}

	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}

	public CacheEntryFacade getFacade() {
		return new CacheEntryFacade(path, excludes);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

}
