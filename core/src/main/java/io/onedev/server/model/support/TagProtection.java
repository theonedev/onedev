package io.onedev.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

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
	
	private String submitter = new Anyone().toString();
	
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

	@Editable(order=150, name="If Performed By", description="This protection rule will apply "
			+ "only if the action is performed by specified users here")
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
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
		submitter = UserMatcher.onRenameGroup(submitter, oldName, newName);
	}
	
	public boolean onDeleteGroup(String groupName) {
		submitter = UserMatcher.onDeleteGroup(submitter, groupName);
		return false;
	}
	
	public void onRenameUser(String oldName, String newName) {
		submitter = UserMatcher.onRenameUser(submitter, oldName, newName);
	}
	
	public boolean onDeleteUser(String userName) {
		submitter = UserMatcher.onDeleteUser(submitter, userName);
		return false;
	}

	public boolean onTagDeleted(String tagName) {
		PatternSet patternSet = PatternSet.fromString(getTags());
		patternSet.getIncludes().remove(tagName);
		patternSet.getExcludes().remove(tagName);
		setTags(patternSet.toString());
		return getTags().length() == 0;
	}
	
}
