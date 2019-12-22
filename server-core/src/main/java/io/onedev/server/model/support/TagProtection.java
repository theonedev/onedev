package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usermatch.Anyone;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@Horizontal
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tags;
	
	private String userMatch = new Anyone().toString();
	
	private boolean noUpdate = true;
	
	private boolean noDeletion = true;
	
	private boolean noCreation = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify space-separated tags to be protected. Use * or ? for wildcard match")
	@Patterns(suggester = "suggestTags")
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		return SuggestionUtils.suggestTags(Project.get(), matchWith);
	}
	
	@Editable(order=150, name="Applicable Users", description="Rule will apply only if the user changing the tag matches criteria specified here")
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getUserMatch() {
		return userMatch;
	}

	public void setUserMatch(String userMatch) {
		this.userMatch = userMatch;
	}

	@Editable(order=200, description="Check this to not allow tag update")
	public boolean isNoUpdate() {
		return noUpdate;
	}

	public void setNoUpdate(boolean noUpdate) {
		this.noUpdate = noUpdate;
	}

	@Editable(order=300, description="Check this to not allow tag deletion")
	public boolean isNoDeletion() {
		return noDeletion;
	}

	public void setNoDeletion(boolean noDeletion) {
		this.noDeletion = noDeletion;
	}

	@Editable(order=400, description="Check this to not allow tag creation")
	public boolean isNoCreation() {
		return noCreation;
	}

	public void setNoCreation(boolean noCreation) {
		this.noCreation = noCreation;
	}

	public void onRenameGroup(String oldName, String newName) {
		userMatch = UserMatch.onRenameGroup(userMatch, oldName, newName);
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingGroup(userMatch, groupName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'");
	}
	
	public void onRenameUser(String oldName, String newName) {
		userMatch = UserMatch.onRenameUser(userMatch, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingUser(userMatch, userName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'");
	}

	public Usage getTagUsage(String tagName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.fromString(getTags());
		if (patternSet.getIncludes().contains(tagName) || patternSet.getExcludes().contains(tagName))
			usage.add("tag protection '" + getTags() + "'");
		return usage;
	}
	
}
