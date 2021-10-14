package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class ProjectImportSource implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private List<ProjectMapping> projectMappings = new ArrayList<>();

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
			String errorMessage = null;
			try {
				Project project = projectManager.initialize(projectMappings.get(i).getOneDevProject());
				if (!project.isNew()) 
					errorMessage = "Project already exists";
			} catch (UnauthorizedException e) {
				errorMessage = e.getMessage();
			}
			if (errorMessage != null) {
				context.buildConstraintViolationWithTemplate(errorMessage)
						.addPropertyNode("projectMappings")
						.addPropertyNode(ProjectMapping.PROP_ONEDEV_PROJECT)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<projectMappings.size(); j++) {
					if (j != i && projectMappings.get(j).getOneDevProject().equals(projectMappings.get(i).getOneDevProject())) {
						context.buildConstraintViolationWithTemplate("Duplicate project")
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
