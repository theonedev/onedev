package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
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
public class YouTrackImportOption implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private List<YouTrackImport> imports = new ArrayList<>();
	
	private List<IssueStateMapping> issueStateMappings = new ArrayList<>();
	
	private List<IssueFieldMapping> issueFieldMappings = new ArrayList<>();
	
	private List<IssueTagMapping> issueTagMappings = new ArrayList<>();
	
	@Editable(order=100, name="Projects to Import")
	@Size(min=1, max=10000, message="No projects to import")
	public List<YouTrackImport> getImports() {
		return imports;
	}

	public void setImports(List<YouTrackImport> imports) {
		this.imports = imports;
	}

	@Editable(order=200, description="Specify how to map YouTrack issue state to OneDev issue state. "
			+ "Unmapped states will use the initial state in OneDev")
	public List<IssueStateMapping> getIssueStateMappings() {
		return issueStateMappings;
	}

	public void setIssueStateMappings(List<IssueStateMapping> issueStateMappings) {
		this.issueStateMappings = issueStateMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue custom fields to OneDev issue "
			+ "custom fields. Unmapped fields will be reflected in issue description")
	public List<IssueFieldMapping> getIssueFieldMappings() {
		return issueFieldMappings;
	}

	public void setIssueFieldMappings(List<IssueFieldMapping> issueFieldMappings) {
		this.issueFieldMappings = issueFieldMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue tags to OneDev issue custom "
			+ "fields. Unmapped tags will be reflected in issue description")
	public List<IssueTagMapping> getIssueTagMappings() {
		return issueTagMappings;
	}

	public void setIssueTagMappings(List<IssueTagMapping> issueTagMappings) {
		this.issueTagMappings = issueTagMappings;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		for (int i=0; i<imports.size(); i++) {
			if (projectManager.find(imports.get(i).getOneDevProject()) != null) {
				context.buildConstraintViolationWithTemplate("Project name already used")
						.addPropertyNode("imports")
						.addPropertyNode(YouTrackImport.PROP_ONEDEV_PROJECT)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<imports.size(); j++) {
					if (j != i && imports.get(j).getOneDevProject().equals(imports.get(i).getOneDevProject())) {
						context.buildConstraintViolationWithTemplate("Duplicate project name")
								.addPropertyNode("imports")
								.addPropertyNode(YouTrackImport.PROP_ONEDEV_PROJECT)
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
