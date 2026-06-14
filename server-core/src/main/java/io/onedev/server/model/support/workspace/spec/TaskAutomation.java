package io.onedev.server.model.support.workspace.spec;

import static io.onedev.server.model.User.Type.AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;

@Editable
public class TaskAutomation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String runTaskCmd;

	private List<String> applicableAis = new ArrayList<>();

	private boolean deleteWorkspaceIfSucceeded = true;

	@Editable(order = 100, name="Command to Run Task", description = """
			Specify command to launch coding agent in headless mode to run assigned task in form of 
			prompt. The prompt is stored in environment variable <code>$TASK_PROMPT</code>
			""")
	@NotEmpty
	public String getRunTaskCmd() {
		return runTaskCmd;
	}

	public void setRunTaskCmd(String runTaskCmd) {
		this.runTaskCmd = runTaskCmd;
	}

	@Editable(order = 200, name = "Applicable AI Users for Task Automation", placeholder = "All AI users", description = """
			Optionally specify applicable AI users to use this workspace spec for task automation. Leave empty to allow all AI users. 
			When a AI user creates workspace to do its job, the first applicable spec will be used.""")
	@UserChoice("getAiUsers")
	public List<String> getApplicableAis() {
		return applicableAis;
	}

	public void setApplicableAis(List<String> applicableAis) {
		this.applicableAis = applicableAis;
	}

	@Editable(order = 300, description = """
			Whether or not to delete workspace if task command completes successfully. Uncheck this if 
			you want to check how the prompt is processed by coding agent after task command completes 
			successfully. Note that workspace will not be deleted automatically if task command fails.""")
	public boolean isDeleteWorkspaceIfSucceeded() {
		return deleteWorkspaceIfSucceeded;
	}

	public void setDeleteWorkspaceIfSucceeded(boolean deleteWorkspaceIfSucceeded) {
		this.deleteWorkspaceIfSucceeded = deleteWorkspaceIfSucceeded;
	}
	
	@SuppressWarnings("unused")
	private static List<User> getAiUsers() {
		var cache = OneDev.getInstance(UserService.class).cloneCache();
		return cache.getUsers(it -> !it.isDisabled() && it.getType() == AI)
				.stream()
				.sorted(cache.comparingDisplayName())
				.collect(Collectors.toList());
	}

	public void onRenameUser(String oldName, String newName) {
		var index = applicableAis.indexOf(oldName);
		if (index != -1)
			applicableAis.set(index, newName);
	}

}
