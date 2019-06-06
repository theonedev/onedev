package io.onedev.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usermatcher.Anyone;
import io.onedev.server.util.usermatcher.UserMatcher;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.TagPatterns;

@Editable
@Horizontal
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tags;
	
	private String user = new Anyone().toString();
	
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
	@TagPatterns
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Editable(order=150, name="Applicable Users", description="Rule will apply only if the user changing the tag matches criteria specified here")
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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
		user = UserMatcher.onRenameGroup(user, oldName, newName);
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatcher.isUsingGroup(user, groupName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'");
	}
	
	public void onRenameUser(String oldName, String newName) {
		user = UserMatcher.onRenameUser(user, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatcher.isUsingUser(user, userName))
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
