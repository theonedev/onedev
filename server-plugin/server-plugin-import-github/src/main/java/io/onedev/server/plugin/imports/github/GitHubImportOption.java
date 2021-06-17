package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
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
	
	private List<IssueLabelMapping> issueLabelMappings = new ArrayList<>();
	
	@Editable(order=100, name="Repositories to Import")
	@Size(min=1, max=10000, message="No repositories to import")
	public List<GitHubImport> getImports() {
		return imports;
	}

	public void setImports(List<GitHubImport> imports) {
		this.imports = imports;
	}

	@Editable(order=200, name="Public Role", description="If specified, all public repositories imported from GitHub "
			+ "will use this as default role. Private repositories are not affected")
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
	
	@Editable(order=350, description="Specify a multi-value user field to "
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

	@Editable(order=400, description="Specify how to map GitHub issue labels to OneDev custom "
			+ "fields. Only multi-valued enum field can be used here. Unmapped labels will be "
			+ "reflected in issue description")
	public List<IssueLabelMapping> getIssueLabelMappings() {
		return issueLabelMappings;
	}

	public void setIssueLabelMappings(List<IssueLabelMapping> issueLabelMappings) {
		this.issueLabelMappings = issueLabelMappings;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		for (int i=0; i<imports.size(); i++) {
			if (projectManager.find(imports.get(i).getOneDevProject()) != null) {
				context.buildConstraintViolationWithTemplate("Project name already used")
						.addPropertyNode("imports")
						.addPropertyNode(GitHubImport.PROP_ONEDEV_PROJECT)
						.inIterable().atIndex(i).addConstraintViolation();
				isValid = false;
			} else {
				for (int j=0; j<imports.size(); j++) {
					if (j != i && imports.get(j).getOneDevProject().equals(imports.get(i).getOneDevProject())) {
						context.buildConstraintViolationWithTemplate("Duplicate project name")
								.addPropertyNode("imports")
								.addPropertyNode(GitHubImport.PROP_ONEDEV_PROJECT)
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
