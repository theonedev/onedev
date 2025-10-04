package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.EditContext;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable(name="Add Issues to Iteration")
public class AddToIterationBean implements Serializable {
	
	public static final String PROP_SEND_NOTIFICATIONS = "sendNotifications";
	private Long projectId;
	
	private String iterationPrefix;
	
	private String currentIteration;
	
	private boolean backlog;
	
	private String iteration;
	
	private boolean removeFromCurrentIteration;
	
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
	public String getCurrentIteration() {
		return currentIteration;
	}

	public void setCurrentIteration(String currentIteration) {
		this.currentIteration = currentIteration;
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

	@Editable(order=150)
	@ShowCondition("isRemoveFromCurrentIterationVisible")
	public boolean isRemoveFromCurrentIteration() {
		return removeFromCurrentIteration;
	}

	public void setRemoveFromCurrentIteration(boolean removeFromCurrentIteration) {
		this.removeFromCurrentIteration = removeFromCurrentIteration;
	}

	@SuppressWarnings("unused")
	private static boolean isRemoveFromCurrentIterationVisible() {
		var editContext = EditContext.get();
		return !(Boolean)editContext.getInputValue("backlog") 
				&& editContext.getInputValue("currentIteration") != null;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getIterationChoices() {
		var projectId = (Long) EditContext.get().getInputValue("projectId");
		var iterationPrefix = (String) EditContext.get().getInputValue("iterationPrefix");
		var currentIteration = (String) EditContext.get().getInputValue("currentIteration");
		var backlog = (boolean) EditContext.get().getInputValue("backlog");
		var project = OneDev.getInstance(ProjectService.class).load(projectId);
		
		var iterations = new ArrayList<String>();
		for (var iteration: project.getSortedHierarchyIterations()) {
			if ((iterationPrefix == null || iteration.getName().startsWith(iterationPrefix))
					&& (backlog || !iteration.getName().equals(currentIteration))) {
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
