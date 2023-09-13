package io.onedev.server.model.support.issue;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.WorkingPeriodField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.util.SuggestionUtils;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Editable
@ClassValidating
public class TimeTrackingSetting implements Validatable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String projects;
	
	private String estimatedTimeField;

	private String spentTimeField;
	
	private List<String> timeSummingLinks;

	@Editable(name="Applicable Projects", order=100, placeholder="All projects", description="" +
			"Optionally specify space-separated projects applicable for time tracking. " +
			"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all projects")
	@Patterns(suggester="suggestProjects", path=true)
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
	
	@Editable(order=300)
	@ChoiceProvider("getWorkingPeriodChoices")
	@NotEmpty
	public String getEstimatedTimeField() {
		return estimatedTimeField;
	}

	public void setEstimatedTimeField(String estimatedTimeField) {
		this.estimatedTimeField = estimatedTimeField;
	}

	@Editable(order=400)
	@ChoiceProvider("getWorkingPeriodChoices")
	@NotEmpty
	public String getSpentTimeField() {
		return spentTimeField;
	}

	public void setSpentTimeField(String spentTimeField) {
		this.spentTimeField = spentTimeField;
	}
	
	private static List<String> getWorkingPeriodChoices() {
		var choices = new ArrayList<String>();
		for (var field: getIssueSetting().getFieldSpecs()) {
			if (field instanceof WorkingPeriodField)
				choices.add(field.getName());
		}
		return choices;
	}

	@Editable(order=500)
	@ChoiceProvider("getLinkChoices")
	public List<String> getTimeSummingLinks() {
		return timeSummingLinks;
	}

	public void setTimeSummingLinks(List<String> timeSummingLinks) {
		this.timeSummingLinks = timeSummingLinks;
	}
	
	private static List<String> getLinkChoices() {
		var choices = new ArrayList<String>();
		for (var linkSpec: OneDev.getInstance(LinkSpecManager.class).query()) {
			if (linkSpec.isMultiple()) 
				choices.add(linkSpec.getName());
			var opposite = linkSpec.getOpposite();
			if (opposite != null && opposite.isMultiple())
				choices.add(opposite.getName());
		}
		return choices;
	}

	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		if (getIssueSetting().getFieldSpec(estimatedTimeField) == null)
			undefinedFields.add(estimatedTimeField);
		if (getIssueSetting().getFieldSpec(spentTimeField) == null)
			undefinedFields.add(spentTimeField);
		return undefinedFields;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (entry.getKey().equals(estimatedTimeField))
					estimatedTimeField = entry.getValue().getNewField();
				if (entry.getKey().equals(spentTimeField))
					spentTimeField = entry.getValue().getNewField();
			} else if (entry.getKey().equals(estimatedTimeField) || entry.getKey().equals(spentTimeField)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (estimatedTimeField != null && estimatedTimeField.equals(spentTimeField)) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Should not use same field")
					.addPropertyNode("estimatedTimeField").addConstraintViolation();
			context.buildConstraintViolationWithTemplate("Should not use same field")
					.addPropertyNode("spentTimeField").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		if (timeSummingLinks.contains(linkName))
			usage.add("time summing links");
		return usage;
	}

	public void onRenameLink(String oldName, String newName) {
		var index = timeSummingLinks.indexOf(oldName);
		if (index != -1)
			timeSummingLinks.set(index, newName);
	}
	
	public static class AutoWorkLogging implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String issueState;
		
		private String workerField;

		@Editable(order=100)
		@ChoiceProvider("getStateChoices")
		@NotEmpty
		public String getIssueState() {
			return issueState;
		}

		public void setIssueState(String issueState) {
			this.issueState = issueState;
		}

		private static List<String> getStateChoices() {
			return getIssueSetting().getStateSpecs().stream()
					.map(StateSpec::getName).collect(toList());
		}
		
		@Editable(order=200)
		@ChoiceProvider("getWorkerChoices")
		@NotEmpty
		public String getWorkerField() {
			return workerField;
		}

		public void setWorkerField(String workerField) {
			this.workerField = workerField;
		}
		
		private static List<String> getWorkerChoices() {
			List<String> choices = new ArrayList<>();
			for (var field: getIssueSetting().getFieldSpecs()) {
				if (field instanceof UserChoiceField || field instanceof GroupChoiceField)
					choices.add(field.getName());
			}
			return choices;
		}
		
	}
}
