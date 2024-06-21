package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.util.EditContext;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable(name="Add Issues to Iteration")
public class AddToIterationBean implements Serializable {
	
	private Long projectId;
	
	private String iterationPrefix;
	
	private String boardIteration;
	
	private boolean backlog;
	
	private String iteration;
	
	private boolean sendNotifications;

	@Editable(hidden = true)
	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Editable(hidden = true)
	public String getIterationPrefix() {
		return iterationPrefix;
	}

	public void setIterationPrefix(String iterationPrefix) {
		this.iterationPrefix = iterationPrefix;
	}

	@Editable(hidden = true)
	public String getBoardIteration() {
		return boardIteration;
	}

	public void setBoardIteration(String boardIteration) {
		this.boardIteration = boardIteration;
	}

	@Editable(hidden = true)
	public boolean isBacklog() {
		return backlog;
	}

	public void setBacklog(boolean backlog) {
		this.backlog = backlog;
	}

	@Editable(order=100, description = "Select iteration to schedule issues into")
	@ChoiceProvider("getIterationChoices")
	@NotEmpty
	public String getIteration() {
		return iteration;
	}

	public void setIteration(String iteration) {
		this.iteration = iteration;
	}
	
	private static List<String> getIterationChoices() {
		var projectId = (Long) EditContext.get().getInputValue("projectId");
		var iterationPrefix = (String) EditContext.get().getInputValue("iterationPrefix");
		var boardIteration = (String) EditContext.get().getInputValue("boardIteration");
		var backlog = (boolean) EditContext.get().getInputValue("backlog");
		var project = OneDev.getInstance(ProjectManager.class).load(projectId);
		
		var iterations = new ArrayList<String>();
		for (var iteration: project.getSortedHierarchyIterations()) {
			if ((iterationPrefix == null || iteration.getName().startsWith(iterationPrefix))
					&& (backlog || !iteration.getName().equals(boardIteration))) {
				iterations.add(iteration.getName());
			}
		}
		return iterations;
	}

	@Editable(order=200, description = "Whether or not to send notifications to issue watchers for this change")
	public boolean isSendNotifications() {
		return sendNotifications;
	}

	public void setSendNotifications(boolean sendNotifications) {
		this.sendNotifications = sendNotifications;
	}
}
