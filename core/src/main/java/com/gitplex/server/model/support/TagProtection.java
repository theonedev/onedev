package com.gitplex.server.model.support;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.model.support.tagcreator.ProjectWriters;
import com.gitplex.server.model.support.tagcreator.SpecifiedGroup;
import com.gitplex.server.model.support.tagcreator.SpecifiedUser;
import com.gitplex.server.model.support.tagcreator.TagCreator;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.TagPattern;

@Editable
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tag;
	
	private boolean noUpdate = true;
	
	private boolean noDeletion = true;
	
	private TagCreator tagCreator = new ProjectWriters();

	@Editable(order=100, description="Specify tag to be protected. Wildcard can be used to match multiple tags")
	@TagPattern
	@NotEmpty
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
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

	@Editable(order=400, name="Allowed Creator", description="Specify users allowed to create specified tag")
	@NotNull
	public TagCreator getTagCreator() {
		return tagCreator;
	}

	public void setTagCreator(TagCreator tagCreator) {
		this.tagCreator = tagCreator;
	}
	
	public void onGroupRename(String oldName, String newName) {
		if (getTagCreator() instanceof SpecifiedGroup) {
			SpecifiedGroup specifiedGroup = (SpecifiedGroup) getTagCreator();
			if (specifiedGroup.getGroupName().equals(oldName))
				specifiedGroup.setGroupName(newName);
		}
	}
	
	public void onGroupDelete(String groupName) {
		if (getTagCreator() instanceof SpecifiedGroup) {
			SpecifiedGroup specifiedGroup = (SpecifiedGroup) getTagCreator();
			if (specifiedGroup.getGroupName().equals(groupName))
				setTagCreator(new ProjectWriters());
		}
	}
	
	public void onUserRename(String oldName, String newName) {
		if (getTagCreator() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getTagCreator();
			if (specifiedUser.getUserName().equals(oldName))
				specifiedUser.setUserName(newName);
		}
	}
	
	public void onUserDelete(String userName) {
		if (getTagCreator() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getTagCreator();
			if (specifiedUser.getUserName().equals(userName))
				setTagCreator(new ProjectWriters());
		}
	}

	public boolean onTagDelete(String tagName) {
		return tagName.equals(getTag());
	}
	
}
