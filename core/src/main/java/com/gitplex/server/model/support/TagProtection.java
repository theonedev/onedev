package com.gitplex.server.model.support;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.tagcreator.DepotWriters;
import com.gitplex.server.model.support.tagcreator.SpecifiedTeam;
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
	
	private TagCreator tagCreator = new DepotWriters();

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
	
	public void onDepotTransferred(Depot depot) {
		if (getTagCreator() instanceof SpecifiedTeam)
			setTagCreator(new DepotWriters());
	}
	
	public void onTeamRename(String oldName, String newName) {
		if (getTagCreator() instanceof SpecifiedTeam) {
			SpecifiedTeam specifiedTeam = (SpecifiedTeam) getTagCreator();
			if (specifiedTeam.getTeamName().equals(oldName))
				specifiedTeam.setTeamName(newName);
		}
	}
	
	public void onTeamDelete(String teamName) {
		if (getTagCreator() instanceof SpecifiedTeam) {
			SpecifiedTeam specifiedTeam = (SpecifiedTeam) getTagCreator();
			if (specifiedTeam.getTeamName().equals(teamName))
				setTagCreator(new DepotWriters());
		}
	}
	
	public void onAccountRename(String oldName, String newName) {
		if (getTagCreator() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getTagCreator();
			if (specifiedUser.getUserName().equals(oldName))
				specifiedUser.setUserName(newName);
		}
	}
	
	public void onAccountDelete(String accountName) {
		if (getTagCreator() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) getTagCreator();
			if (specifiedUser.getUserName().equals(accountName))
				setTagCreator(new DepotWriters());
		}
	}

	public boolean onTagDelete(String tagName) {
		return tagName.equals(getTag());
	}
	
}
