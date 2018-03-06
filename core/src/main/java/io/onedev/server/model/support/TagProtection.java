package io.onedev.server.model.support;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.submitter.Anyone;
import io.onedev.server.model.support.submitter.SpecifiedGroup;
import io.onedev.server.model.support.submitter.SpecifiedUser;
import io.onedev.server.model.support.submitter.Submitter;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.TagPattern;

@Editable
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tag;
	
	private Submitter submitter = new Anyone();
	
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
	@NotNull
	public Submitter getSubmitter() {
		return submitter;
	}

	public void setSubmitter(Submitter submitter) {
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

	public void onGroupRename(String oldName, String newName) {
		if (getSubmitter() instanceof SpecifiedGroup) {
			SpecifiedGroup specifiedGroup = (SpecifiedGroup) getSubmitter();
			if (specifiedGroup.getGroupName().equals(oldName))
				specifiedGroup.setGroupName(newName);
		}
	}
	
	public boolean onGroupDelete(String groupName) {
		if (getSubmitter() instanceof SpecifiedGroup) {
			SpecifiedGroup specifiedGroup = (SpecifiedGroup) getSubmitter();
			if (specifiedGroup.getGroupName().equals(groupName))
				return true;
		}
		return false;
	}
	
	public void onUserRename(String oldName, String newName) {
		if (getSubmitter() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getSubmitter();
			if (specifiedUser.getUserName().equals(oldName))
				specifiedUser.setUserName(newName);
		}
	}
	
	public boolean onUserDelete(String userName) {
		if (getSubmitter() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getSubmitter();
			if (specifiedUser.getUserName().equals(userName))
				return true;
		}
		return false;
	}

	public boolean onTagDelete(String tagName) {
		return tagName.equals(getTag());
	}
	
}
