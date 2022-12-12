package io.onedev.server.model.support.build.actionauthorization;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=20, name="Create tag")
public class CreateTagAuthorization extends ActionAuthorization {

	private static final long serialVersionUID = 1L;

	private String tagNames;

	@Editable(order=100, placeholder="All", description="Specify space-separated tag names. "
			+ "Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestTags", path=true)
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
	public String getActionDescription() {
		if (tagNames != null)
			return "Create tag with name matching '" + tagNames + "'";
		else
			return "Create tag";
	}
	
}
