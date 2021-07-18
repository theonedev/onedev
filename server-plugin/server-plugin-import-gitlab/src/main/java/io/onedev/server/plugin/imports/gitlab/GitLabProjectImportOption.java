package io.onedev.server.plugin.imports.gitlab;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable
@ClassValidating
public class GitLabProjectImportOption extends GitLabIssueImportOption implements Validatable {

	private static final long serialVersionUID = 1L;

	private List<ProjectMapping> projectMappings = new ArrayList<>();
	
	private String publicRoleName;
	
	@Editable(order=200, name="Public Role", description="If specified, all public and internal projects imported from GitLab "
			+ "will use this as default role. Private projects are not affected")
	@RoleChoice
	@Nullable
	public String getPublicRoleName() {
		return publicRoleName;
	}

	public void setPublicRoleName(String publicRoleName) {
		this.publicRoleName = publicRoleName;
	}
	
	@Nullable
	public Role getPublicRole() {
		if (publicRoleName != null)
			return OneDev.getInstance(RoleManager.class).find(publicRoleName);
		else
			return null;
	}

	@Editable(order=100, name="Projects to Import")
	@Size(min=1, max=10000, message="No projects to import")
	public List<ProjectMapping> getProjectMappings() {
		return projectMappings;
	}

	public void setProjectMappings(List<ProjectMapping> projectMappings) {
		this.projectMappings = projectMappings;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		for (int i=0; i<projectMappings.size(); i++) {
			if (projectManager.find(projectMappings.get(i).getOneDevProject()) != null) {
				context.buildConstraintViolationWithTemplate("Project name already used")
						.addPropertyNode("projectMappings")
						.addPropertyNode(ProjectMapping.PROP_ONEDEV_PROJECT)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<projectMappings.size(); j++) {
					if (j != i && projectMappings.get(j).getOneDevProject().equals(projectMappings.get(i).getOneDevProject())) {
						context.buildConstraintViolationWithTemplate("Duplicate project name")
								.addPropertyNode("projectMappings")
								.addPropertyNode(ProjectMapping.PROP_ONEDEV_PROJECT)
								.inIterable().atIndex(i).addConstraintViolation();
						isValid = false;
						break;
					}
				}
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}

}
