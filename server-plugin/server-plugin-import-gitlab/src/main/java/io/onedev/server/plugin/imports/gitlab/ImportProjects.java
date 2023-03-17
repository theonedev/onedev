package io.onedev.server.plugin.imports.gitlab;

import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanEditor;

import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;

@Editable
public class ImportProjects extends ImportGroup {

	private static final long serialVersionUID = 1L;

	private String parentOneDevProject;
	
	private boolean all;
	
	private boolean includeForks;
	
	private List<String> gitLabProjects;

	@Editable(order=200, name="Parent OneDev Project", description = "Optionally specify a OneDev project " +
			"to be used as parent of imported projects. Leave empty to import as root projects")
	@ProjectChoice
	public String getParentOneDevProject() {
		return parentOneDevProject;
	}

	public void setParentOneDevProject(String parentOneDevProject) {
		this.parentOneDevProject = parentOneDevProject;
	}
	
	@Editable(order=300, name="Import All Projects")
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}
	
	private static boolean isAllEnabled() {
		return (Boolean)EditContext.get().getInputValue("all");
	}

	private static boolean isAllDisabled() {
		return !isAllEnabled();
	}
	
	@Editable(order=400, description="Whether or not to import forked GitLab projects")
	@ShowCondition("isAllEnabled")
	public boolean isIncludeForks() {
		return includeForks;
	}

	public void setIncludeForks(boolean includeForks) {
		this.includeForks = includeForks;
	}

	@Editable(order=500, name="GitLab Projects to Import")
	@ChoiceProvider("getGitLabProjectChoices")
	@ShowCondition("isAllDisabled")
	@Size(min=1, message="At least one project should be selected")
	public List<String> getGitLabProjects() {
		return gitLabProjects;
	}

	public void setGitLabProjects(List<String> gitLabProjects) {
		this.gitLabProjects = gitLabProjects;
	}

	private static List<String> getGitLabProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProjects projects = (ImportProjects) editor.getModelObject();
		String groupId = (String) EditContext.get().getInputValue("groupId");
		return projects.server.listProjects(groupId, true);
	}
	
	public Collection<String> getImportProjects() {
		if (isAll()) 
			return server.listProjects(getGroupId(), isIncludeForks());
		else
			return getGitLabProjects();	
	} 
	
}
