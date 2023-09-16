package io.onedev.server.model.support.issue;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.WorkingPeriodField;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.util.SuggestionUtils;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.*;

@Editable
@ClassValidating
public class TimeTrackingSetting implements Validatable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String projects;
	
	private String estimatedTimeField;

	private String spentTimeField;
	
	private String timeAggregationLink;
	
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
	public String getTimeAggregationLink() {
		return timeAggregationLink;
	}

	public void setTimeAggregationLink(String timeAggregationLink) {
		this.timeAggregationLink = timeAggregationLink;
	}
	
	private static List<String> getLinkChoices() {
		var choices = new LinkedHashSet<String>();
		for (var linkSpec: OneDev.getInstance(LinkSpecManager.class).query()) {
			if (linkSpec.getOpposite() != null) {
				choices.add(linkSpec.getName());
				choices.add(linkSpec.getOpposite().getName());
			}
		}
		return new ArrayList<>(choices);
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
	
	public boolean isProjectApplicable(Project project) {
		return projects == null || PatternSet.parse(projects).matches(new PathMatcher(), project.getPath());
	}

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		if (linkName.equals(timeAggregationLink))
			usage.add("time aggregation link");
		return usage;
	}

	public void onRenameLink(String oldName, String newName) {
		if (oldName.equals(timeAggregationLink))
			timeAggregationLink = newName;
	}
	
}
