package io.onedev.server.plugin.imports.bitbucketcloud;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.editable.BeanEditor;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Editable
@ClassValidating
public class ImportRepositories extends ImportWorkspace implements Validatable {

	private static final long serialVersionUID = 1L;

	private String parentOneDevProject;

	private boolean all;

	private boolean includeForks;

	private List<String> bitbucketRepositories;

	@Editable(order=200, name="Parent OneDev Project", description = "Optionally specify a OneDev project " +
			"to be used as parent of imported repositories. Leave empty to import as root projects")
	@ChoiceProvider("getParentOneDevProjectChoices")
	public String getParentOneDevProject() {
		return parentOneDevProject;
	}

	public void setParentOneDevProject(String parentOneDevProject) {
		this.parentOneDevProject = parentOneDevProject;
	}

	private static List<String> getParentOneDevProjectChoices() {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		return projectManager.getPermittedProjects(new CreateChildren()).stream()
				.map(it->it.getPath()).sorted().collect(Collectors.toList());
	}

	@Editable(order=300, name="Import All Repositories")
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

	@Editable(order=400, description="Whether or not to import forked Bitbucket repositories")
	@ShowCondition("isAllEnabled")
	public boolean isIncludeForks() {
		return includeForks;
	}

	public void setIncludeForks(boolean includeForks) {
		this.includeForks = includeForks;
	}

	@Editable(order=500, name="Bitbucket Repositories to Import")
	@ChoiceProvider("getBitbucketRepositoryChoices")
	@ShowCondition("isAllDisabled")
	@Size(min=1, message="At least one repository should be selected")
	public List<String> getBitbucketRepositories() {
		return bitbucketRepositories;
	}

	public void setBitbucketRepositories(List<String> bitbucketRepositories) {
		this.bitbucketRepositories = bitbucketRepositories;
	}

	private static List<String> getBitbucketRepositoryChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportRepositories repositories = (ImportRepositories) editor.getModelObject();
		String workspace = (String) EditContext.get().getInputValue("workspace");
		return repositories.server.listRepositories(workspace, true);
	}

	public Collection<String> getImportRepositories() {
		if (isAll())
			return server.listRepositories(getWorkspace(), isIncludeForks());
		else
			return getBitbucketRepositories();
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
