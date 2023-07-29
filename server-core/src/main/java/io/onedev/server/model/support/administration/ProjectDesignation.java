package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;

@Editable
public class ProjectDesignation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String senderEmails;
	
	private String project;

	@Editable(order=100, name="Applicable Senders", placeholder="Any sender", description=""
			+ "Specify space-separated sender email addresses applicable for this entry. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. "
			+ "Leave empty to match all senders")
	@Patterns
	public String getSenderEmails() {
		return senderEmails;
	}

	public void setSenderEmails(String senderEmails) {
		this.senderEmails = senderEmails;
	}
	
	@Editable(order=200, name="Default Project")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> projectPaths = OneDev.getInstance(ProjectManager.class)
				.query().stream().map(it->it.getPath()).collect(Collectors.toList());
		Collections.sort(projectPaths);
		return projectPaths;
	}
		
}