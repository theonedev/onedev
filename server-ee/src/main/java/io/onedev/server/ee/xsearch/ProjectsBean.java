package io.onedev.server.ee.xsearch;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.web.util.SuggestionUtils;

import java.io.Serializable;
import java.util.List;

@Editable
public class ProjectsBean implements Serializable {
	
	private String projects;

	@Editable(name="In Projects", order=100, placeholder="All projects with code read permission", description="" +
			"Optionally specify space-separated projects to search in. Use '**', '*' or '?' " +
			"for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. " +
			"Prefix with '-' to exclude. Leave empty to search in all projects with code read permission")
	@Patterns(suggester="suggestProjects", path=true)
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith, new ReadCode());
	}
	
}
