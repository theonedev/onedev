package io.onedev.server.model.support.build.actionauthorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public abstract class ActionAuthorization implements Serializable {

	private static final long serialVersionUID = 1L;

	private String authorizedBranches;

	@Editable(order=1000, description="Action is allowed only if build runs on "
			+ "specified branches. Multiple branches should be separated with space. "
			+ "Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("All")
	public String getAuthorizedBranches() {
		return authorizedBranches;
	}

	public void setAuthorizedBranches(String authorizedBranches) {
		this.authorizedBranches = authorizedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	public abstract boolean matches(PostBuildAction postBuildAction);
	
	public abstract String getActionDescription();
	
}
