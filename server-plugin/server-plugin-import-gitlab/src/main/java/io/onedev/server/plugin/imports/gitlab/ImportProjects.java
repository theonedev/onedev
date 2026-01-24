package io.onedev.server.plugin.imports.gitlab;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.editable.BeanEditor;

@Editable
@ClassValidating
public class ImportProjects extends ImportGroup implements Validatable {

	private static final long serialVersionUID = 1L;

	private String parentOneDevProject;
	
	private boolean all;
	
	private boolean includeForks;
	
	private List<String> gitLabProjects;

	@Editable(order=200, name="Parent OneDev Project", description = "Optionally specify a OneDev project " +
			"to be used as parent of imported projects. Leave empty to import as root projects")
	@ChoiceProvider("getParentOneDevProjectChoices")
	public String getParentOneDevProject() {
		return parentOneDevProject;
	}

	public void setParentOneDevProject(String parentOneDevProject) {
		this.parentOneDevProject = parentOneDevProject;
	}

	@SuppressWarnings("unused")
	private static List<String> getParentOneDevProjectChoices() {
		return SecurityUtils.getAuthorizedProjects(new CreateChildren()).stream()
				.map(it->it.getPath()).sorted().collect(Collectors.toList());
	}
	
	@Editable(order=300, name="Import All Projects")
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	@Editable(order=400, description="Whether or not to import forked GitLab projects")
	@DependsOn(property="all")
	public boolean isIncludeForks() {
		return includeForks;
	}

	public void setIncludeForks(boolean includeForks) {
		this.includeForks = includeForks;
	}

	@Editable(order=500, name="GitLab Projects to Import")
	@ChoiceProvider("getGitLabProjectChoices")
	@DependsOn(property="all", value="false")
	@Size(min=1, message="At least one project should be selected")
	public List<String> getGitLabProjects() {
		return gitLabProjects;
	}

	public void setGitLabProjects(List<String> gitLabProjects) {
		this.gitLabProjects = gitLabProjects;
	}

	@SuppressWarnings("unused")
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
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (parentOneDevProject == null && !SecurityUtils.canCreateRootProjects()) {
			context.disableDefaultConstraintViolation();
			var errorMessage = "No permission to import as root projects, please specify parent project";
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode("parentOneDevProject")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
