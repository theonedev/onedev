package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable
@ClassValidating
public class GitHubImportOption implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private List<GitHubImport> imports = new ArrayList<>();
	
	private String publicRoleName;
	
	private String closedIssueState;
	
	private String assigneesIssueField;
	
	private List<LabelMapping> labelMappings = new ArrayList<>();
	
	@Editable(order=100, name="Repositories to Import")
	@Size(min=1, max=10000, message="No repositories to import")
	public List<GitHubImport> getImports() {
		return imports;
	}

	public void setImports(List<GitHubImport> imports) {
		this.imports = imports;
	}

	@Editable(order=200, name="Public Role", description="Specify default role for public repositories")
	@RoleChoice
	@NotEmpty
	public String getPublicRoleName() {
		return publicRoleName;
	}

	public void setPublicRoleName(String publicRoleName) {
		this.publicRoleName = publicRoleName;
	}
	
	public Role getPublicRole() {
		return OneDev.getInstance(RoleManager.class).find(publicRoleName);
	}

	@Editable(order=300, description="Specify which issue state to use for closed GitHub issues")
	@ChoiceProvider("getCloseStateChoices")
	@NotEmpty
	public String getClosedIssueState() {
		return closedIssueState;
	}

	public void setClosedIssueState(String closedIssueState) {
		this.closedIssueState = closedIssueState;
	}

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getCloseStateChoices() {
		List<String> choices = getIssueSetting().getStateSpecs().stream()
				.map(it->it.getName()).collect(Collectors.toList());
		choices.remove(0);
		return choices;
	}
	
	@Editable(order=350, description="Specify a multi-value issue custom field of type user to "
			+ "hold assignees information")
	@ChoiceProvider("getAssigneesIssueFieldChoices")
	@NotEmpty
	public String getAssigneesIssueField() {
		return assigneesIssueField;
	}

	public void setAssigneesIssueField(String assigneesIssueField) {
		this.assigneesIssueField = assigneesIssueField;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAssigneesIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.USER) && field.isAllowMultiple())
				choices.add(field.getName());
		}
		return choices;
	}

	@Editable(order=400, description="Map GitHub issue label to OneDev issue custom field of enum type")
	public List<LabelMapping> getLabelMappings() {
		return labelMappings;
	}

	public void setLabelMappings(List<LabelMapping> labelMappings) {
		this.labelMappings = labelMappings;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		for (int i=0; i<imports.size(); i++) {
			if (projectManager.find(imports.get(i).getProjectName()) != null) {
				context.buildConstraintViolationWithTemplate("Project name already used")
						.addPropertyNode("imports")
						.addPropertyNode(GitHubImport.PROP_PROJECT_NAME)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<imports.size(); j++) {
					if (j != i && imports.get(j).getProjectName().equals(imports.get(i).getProjectName())) {
						context.buildConstraintViolationWithTemplate("Duplicate project name")
								.addPropertyNode("imports")
								.addPropertyNode(GitHubImport.PROP_PROJECT_NAME)
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
