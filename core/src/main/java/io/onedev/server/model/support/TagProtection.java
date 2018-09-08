package io.onedev.server.model.support;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.usermatcher.Anyone;
import io.onedev.server.model.support.usermatcher.SpecifiedTeam;
import io.onedev.server.model.support.usermatcher.SpecifiedUser;
import io.onedev.server.model.support.usermatcher.UserMatcher;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TagPattern;

@Editable
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tag;
	
	private UserMatcher submitter = new Anyone();
	
	private boolean noUpdate = true;
	
	private boolean noDeletion = true;
	
	private boolean noCreation = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100, description="Specify tag to be protected. Wildcard can be used to match multiple tags")
	@TagPattern
	@NotEmpty
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Editable(order=150, name="If Performed By", description="This protection rule will apply "
			+ "only if the action is performed by specified users here")
	@NotNull(message="may not be empty")
	public UserMatcher getSubmitter() {
		return submitter;
	}

	public void setSubmitter(UserMatcher submitter) {
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
		if (getSubmitter() instanceof SpecifiedTeam) {
			SpecifiedTeam specifiedGroup = (SpecifiedTeam) getSubmitter();
			if (specifiedGroup.getTeamName().equals(oldName))
				specifiedGroup.setTeamName(newName);
		}
	}
	
	public boolean onDeleteGroup(String groupName) {
		if (getSubmitter() instanceof SpecifiedTeam) {
			SpecifiedTeam specifiedGroup = (SpecifiedTeam) getSubmitter();
			if (specifiedGroup.getTeamName().equals(groupName))
				return true;
		}
		return false;
	}
	
	public void onRenameUser(String oldName, String newName) {
		if (getSubmitter() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getSubmitter();
			if (specifiedUser.getUserName().equals(oldName))
				specifiedUser.setUserName(newName);
		}
	}
	
	public boolean onDeleteUser(String userName) {
		if (getSubmitter() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getSubmitter();
			if (specifiedUser.getUserName().equals(userName))
				return true;
		}
		return false;
	}

	public boolean onTagDeleted(String tagName) {
		return tagName.equals(getTag());
	}
	
}
