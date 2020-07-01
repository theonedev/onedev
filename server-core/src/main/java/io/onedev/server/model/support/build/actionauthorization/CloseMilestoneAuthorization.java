package io.onedev.server.model.support.build.actionauthorization;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.action.CloseMilestoneAction;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="Close milestone")
public class CloseMilestoneAuthorization extends ActionAuthorization {

	private static final long serialVersionUID = 1L;

	private String milestoneNames;

	@Editable(order=100, description="Specify space-separated milestone names. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestMilestones")
	@NameOfEmptyValue("All")
	public String getMilestoneNames() {
		return milestoneNames;
	}

	public void setMilestoneNames(String milestoneNames) {
		this.milestoneNames = milestoneNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestMilestones(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestMilestones(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Override
	public boolean matches(PostBuildAction postBuildAction) {
		if (postBuildAction instanceof CloseMilestoneAction) {
			CloseMilestoneAction closeMilestoneAction = (CloseMilestoneAction) postBuildAction;
			return milestoneNames == null || WildcardUtils.matchPath(milestoneNames, closeMilestoneAction.getMilestoneName());
		} else {
			return false;
		}
	}

	@Override
	public String getActionDescription() {
		if (milestoneNames != null)
			return "Close milestone with name matching '" + milestoneNames + "'";
		else
			return "Close milestone";
	}

}
