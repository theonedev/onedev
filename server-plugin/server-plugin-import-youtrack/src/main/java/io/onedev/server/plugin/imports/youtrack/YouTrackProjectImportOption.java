package io.onedev.server.plugin.imports.youtrack;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class YouTrackProjectImportOption extends YouTrackIssueImportOption implements Validatable {

	private static final long serialVersionUID = 1L;

	private List<ProjectMapping> projecMappings = new ArrayList<>();
	
	@Editable(order=100, name="Projects to Import")
	@Size(min=1, max=10000, message="No projects to import")
	public List<ProjectMapping> getProjectMappings() {
		return projecMappings;
	}

	public void setProjectMappings(List<ProjectMapping> projectMappings) {
		this.projecMappings = projectMappings;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		for (int i=0; i<projecMappings.size(); i++) {
			if (projectManager.find(projecMappings.get(i).getOneDevProject()) != null) {
				context.buildConstraintViolationWithTemplate("Project name already used")
						.addPropertyNode("projectMappings")
						.addPropertyNode(ProjectMapping.PROP_ONEDEV_PROJECT)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<projecMappings.size(); j++) {
					if (j != i && projecMappings.get(j).getOneDevProject().equals(projecMappings.get(i).getOneDevProject())) {
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
