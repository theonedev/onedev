package io.onedev.server.model.support.build.actionauthorization;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.action.CreateTagAction;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=100, name="Create tag")
public class CreateTagAuthorization extends ActionAuthorization {

	private static final long serialVersionUID = 1L;

	private String tagNames;

	@Editable(order=100, description="Specify space-separated tag names. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestTags", path=true)
	@NameOfEmptyValue("All")
	public String getTagNames() {
		return tagNames;
	}

	public void setTagNames(String tagNames) {
		this.tagNames = tagNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestTags(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Override
	public boolean matches(PostBuildAction postBuildAction) {
		if (postBuildAction instanceof CreateTagAction) {
			CreateTagAction createTagAction = (CreateTagAction) postBuildAction;
			return tagNames == null || WildcardUtils.matchPath(tagNames, createTagAction.getTagName());
		} else {
			return false;
		}
	}
	
	@Override
	public String getActionDescription() {
		if (tagNames != null)
			return "Create tag with name matching '" + tagNames + "'";
		else
			return "Create tag";
	}
	
}
