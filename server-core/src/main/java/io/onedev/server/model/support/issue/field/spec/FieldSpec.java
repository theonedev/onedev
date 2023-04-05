package io.onedev.server.model.support.issue.field.spec;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ShowCondition;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ValueIsNotAnyOf;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.FieldName;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.wicket.MarkupContainer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.*;

@Editable
public abstract class FieldSpec extends InputSpec {
	
	private static final long serialVersionUID = 1L;
	
	private String nameOfEmptyValue;
	
	private boolean promptUponIssueOpen = true;
	
	private String applicableProjects;
	
	private transient Collection<String> dependencies;
	
	private transient Collection<String> dependents;
	
	@Editable(order=10)
	@FieldName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Editable(order=30, placeholder="No description", description="Optionally describes the custom field. " +
			"Html tags are accepted")
	@Multiline
	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

	@Editable(order=35, description="Whether or not multiple values can be specified for this field")
	@Override
	public boolean isAllowMultiple() {
		return super.isAllowMultiple();
	}

	@Override
	public void setAllowMultiple(boolean allowMultiple) {
		super.setAllowMultiple(allowMultiple);
	}

	@Editable(order=40, name="Show Conditionally", placeholder="Always", description="Enable if visibility "
			+ "of this field depends on other fields")
	@Valid
	@Override
	public ShowCondition getShowCondition() {
		return super.getShowCondition();
	}

	@Override
	public void setShowCondition(ShowCondition showCondition) {
		super.setShowCondition(showCondition);
	}
	
	@Editable(order=50, name="Allow Empty Value", description="Whether or not this field accepts empty value")
	@Override
	public boolean isAllowEmpty() {
		return super.isAllowEmpty();
	}

	@Override
	public void setAllowEmpty(boolean allowEmpty) {
		super.setAllowEmpty(allowEmpty);
	}
	
	@Editable(order=60)
	@io.onedev.server.annotation.ShowCondition("isNameOfEmptyValueVisible")
	@NotEmpty
	public String getNameOfEmptyValue() {
		return nameOfEmptyValue;
	}

	public void setNameOfEmptyValue(String nameOfEmptyValue) {
		this.nameOfEmptyValue = nameOfEmptyValue;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNameOfEmptyValueVisible() {
		return (boolean) EditContext.get().getInputValue("allowEmpty");
	}

	@Editable(order=10000, name="Include When Issue is Opened", description="Whether or not to include this field when issue is initially opened. " +
			"If not, you may include this field later when issue is transited to other states via issue transition rule")
	public boolean isPromptUponIssueOpen() {
		return promptUponIssueOpen;
	}

	public void setPromptUponIssueOpen(boolean promptUponIssueOpen) {
		this.promptUponIssueOpen = promptUponIssueOpen;
	}
	
	@SuppressWarnings("unused")
	private static boolean isPromptUponIssueOpenEnabled() {
		return Boolean.TRUE.equals(EditContext.get().getInputValue("promptUponIssueOpen"));
	}

	@Editable(order=10100, placeholder="All projects", description="Specify applicable projects for above option. "
			+ "Multiple projects should be separated by space. Use '**', '*' or '?' for "
			+ "<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty for all projects")
	@io.onedev.server.annotation.ShowCondition("isPromptUponIssueOpenEnabled")
	@Patterns(suggester="suggestProjects", path=true)
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null && getIssueSetting().getFieldSpec(showCondition.getInputName()) == null)
			undefinedFields.add(showCondition.getInputName());
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			FieldSpec field = getIssueSetting().getFieldSpec(showCondition.getInputName());
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(field);
			if (specifiedChoices != null) {
				if (showCondition.getValueMatcher() instanceof ValueIsOneOf) {
					ValueIsOneOf valueIsOneOf = (ValueIsOneOf) showCondition.getValueMatcher(); 
					for (String value: valueIsOneOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(field.getName(), value));
					}
				} else if (showCondition.getValueMatcher() instanceof ValueIsNotAnyOf) {
					ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) showCondition.getValueMatcher(); 
					for (String value: valueIsNotAnyOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(field.getName(), value));
					}
				}
			}
		}
		return undefinedFieldValues;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
				if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
					if (entry.getKey().equals(showCondition.getInputName()))
						showCondition.setInputName(entry.getValue().getNewField());
				} else if (showCondition.getInputName().equals(entry.getKey())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			for (Map.Entry<String, UndefinedFieldValuesResolution> resolutionEntry: resolutions.entrySet()) {
				if (showCondition.getInputName().equals(resolutionEntry.getKey())) {
					if (showCondition.getValueMatcher() instanceof ValueIsOneOf) {
						ValueIsOneOf valueIsOneOf = (ValueIsOneOf) showCondition.getValueMatcher(); 
						valueIsOneOf.getValues().removeAll(resolutionEntry.getValue().getDeletions());
						for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
							int index = valueIsOneOf.getValues().indexOf(renameEntry.getKey());
							if (index != -1) {
								if (valueIsOneOf.getValues().contains(renameEntry.getValue()))
									valueIsOneOf.getValues().remove(index);
								else
									valueIsOneOf.getValues().set(index, renameEntry.getValue());
							}
						}
						if (valueIsOneOf.getValues().isEmpty())
							return false;
					} else if (showCondition.getValueMatcher() instanceof ValueIsNotAnyOf) {
						ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) showCondition.getValueMatcher();
						valueIsNotAnyOf.getValues().removeAll(resolutionEntry.getValue().getDeletions());
						for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
							int index = valueIsNotAnyOf.getValues().indexOf(renameEntry.getKey());
							if (index != -1) {
								if (valueIsNotAnyOf.getValues().contains(renameEntry.getValue()))
									valueIsNotAnyOf.getValues().remove(index);
								else
									valueIsNotAnyOf.getValues().set(index, renameEntry.getValue());
							}
						}
						if (valueIsNotAnyOf.getValues().isEmpty())
							return false;
					}
				}
			}
		}
		return true;
	}
		
	public void onRenameUser(String oldName, String newName) {
	}
	
	public void onRenameGroup(String oldName, String newName) {
		
	}

	public Usage onDeleteUser(String userName) {
		return new Usage();
	}
	
	public Usage onDeleteGroup(String groupName) {
		return new Usage();
	}
	
	public Collection<String> getDependencies() {
		if (dependencies == null) {
			dependencies = new HashSet<>();
			if (getShowCondition() != null)
				dependencies.add(getShowCondition().getInputName());
			
			class PropertyComponent extends MarkupContainer implements InputContext, EditContext {

				private static final long serialVersionUID = 1L;

				public PropertyComponent() {
					super("component");
				}

				@Override
				public List<String> getInputNames() {
					return getIssueSetting().getFieldNames();
				}

				@Override
				public InputSpec getInputSpec(String inputName) {
					return getIssueSetting().getFieldSpec(inputName);
				}

				@Override
				public Object getInputValue(String name) {
					dependencies.add(name);
					return null;
				}

			}	
			
			ComponentContext.push(new ComponentContext(new PropertyComponent()));
			try {
				runScripts();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ComponentContext.pop();
			}
		}
		return dependencies;
	}
	
	protected abstract void runScripts();
	
	public Collection<String> getTransitiveDependencies() {
		return getTransitiveDependencies(new HashSet<>());
	}

	private Collection<String> getTransitiveDependencies(Set<String> checkedFields) {
		Collection<String> transitiveDependencies = new HashSet<>(getDependencies());
		for (String dependency: getDependencies()) {
			if (checkedFields.add(dependency)) { 
				FieldSpec dependencyField = getIssueSetting().getFieldSpec(dependency);
				if (dependencyField != null)
					transitiveDependencies.addAll(dependencyField.getTransitiveDependencies(checkedFields));
			}
		}
		return transitiveDependencies;
	}
	
	public Collection<String> getDependents() {
		if (dependents == null) {
			dependents = new HashSet<>();
			for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
				if (field.getDependencies().contains(getName()))
					dependents.add(field.getName());
			}
		}
		return dependents;
	}
	
	public Collection<String> getTransitiveDependents() {
		return getTransitiveDependents(new HashSet<>());
	}

	private Collection<String> getTransitiveDependents(Set<String> checkedFields) {
		Collection<String> transitiveDependents = new HashSet<>(getDependents());
		for (String dependent: getDependents()) {
			if (checkedFields.add(dependent)) { 
				FieldSpec dependentField = getIssueSetting().getFieldSpec(dependent);
				if (dependentField != null)
					transitiveDependents.addAll(dependentField.getTransitiveDependents(checkedFields));
			}
		}
		return transitiveDependents;
	}

	public void onMoveProject(String oldPath, String newPath) {
		setApplicableProjects(Project.substitutePath(getApplicableProjects(), oldPath, newPath));
	}

	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (Project.containsPath(getApplicableProjects(), projectPath))
			usage.add("applicable projects");
		onDeleteProject(usage, projectPath);
		
		return usage.prefix("custom field '" + getName() + "'");
	}
	
	protected void onDeleteProject(Usage usage, String projectPath) {
	}
	
}
