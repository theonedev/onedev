package io.onedev.server.model.support.pullrequest;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.usage.Usage;

@Editable
public class ProjectPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries;
	
	private MergeStrategy defaultMergeStrategy;
	
	private List<String> defaultAssignees = new ArrayList<>();
	
	private Boolean deleteSourceBranchAfterMerge;
	
	@Nullable
	@Valid
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@SuppressWarnings("unused")
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return _T("Whether or not to fetch LFS objects if pull request is opened from a different project. " +
					"If this option is enabled, git lfs command needs to be installed on OneDev server");
		} else {
			return _T("Whether or not to fetch LFS objects if pull request is opened from a different project.");
		}
	}
	
	@Editable(order=200, placeholder = "Inherit from parent", rootPlaceholder = "Create merge commit", 
			description = "Specify default merge strategy of pull requests submitted to this project")
	public MergeStrategy getDefaultMergeStrategy() {
		return defaultMergeStrategy;
	}

	public void setDefaultMergeStrategy(MergeStrategy defaultMergeStrategy) {
		this.defaultMergeStrategy = defaultMergeStrategy;
	}

	@Editable(order=300, placeholder = "Inherit from parent", rootPlaceholder = "Not assigned", description = "" +
			"Specify default assignees of pull requests submitted to this project. " +
			"Only users with the write code permission to the project can be selected")
	@UserChoice("getAssigneeChoices")
	public List<String> getDefaultAssignees() {
		return defaultAssignees;
	}

	public void setDefaultAssignees(List<String> defaultAssignees) {
		this.defaultAssignees = defaultAssignees;
	}
	
	@SuppressWarnings("unused")
	private static List<User> getAssigneeChoices() {
		var choices = new ArrayList<>(SecurityUtils.getAuthorizedUsers(Project.get(), new WriteCode()));
		Collections.sort(choices, Comparator.comparing(User::getDisplayName));
		return choices;
	}

	@Editable(order=400, placeholder = "Inherit from parent", rootPlaceholder = "No", description = "" +
			"If enabled, source branch will be deleted automatically after merge the pull request if " +
			"user has permission to do that")
	public Boolean getDeleteSourceBranchAfterMerge() {
		return deleteSourceBranchAfterMerge;
	}

	public void setDeleteSourceBranchAfterMerge(Boolean deleteSourceBranchAfterMerge) {
		this.deleteSourceBranchAfterMerge = deleteSourceBranchAfterMerge;
	}

	public void onRenameUser(String oldName, String newName) {
		var index = getDefaultAssignees().indexOf(oldName);
		if (index != -1)
			getDefaultAssignees().set(index, newName);
	}

	public Usage onDeleteUser(String name) {
		Usage usage = new Usage();
		if (getDefaultAssignees().contains(name)) 
			usage.add("default assignees");
		return usage.prefix("pull request").prefix("code");
	}
	
}
