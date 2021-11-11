package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class IssueEffortSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String effortEstimatedField;
	
	@Editable(order=100, description="Specify a field of type 'Working Period' recording estimated effort "
			+ "of the issue")
	@ChoiceProvider("getWorkingPeriodFieldChoices")
	@NotEmpty
	public String getEffortEstimatedField() {
		return effortEstimatedField;
	}

	public void setEffortEstimatedField(String effortEstimatedField) {
		this.effortEstimatedField = effortEstimatedField;
	}

	@SuppressWarnings("unused")
	private static List<String> getWorkingPeriodFieldChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpecs()
				.stream()
				.filter(it->it.getType().equals(InputSpec.WORKING_PERIOD))
				.map(it->it.getName())
				.collect(Collectors.toList());
	}

	private GlobalIssueSetting getIssueSeting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public Set<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		if (getIssueSeting().getFieldSpec(getEffortEstimatedField()) == null)
			undefinedFields.add(getEffortEstimatedField());
		return undefinedFields;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			UndefinedFieldResolution resolution = entry.getValue();
			if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (getEffortEstimatedField().equals(entry.getKey()))
					setEffortEstimatedField(resolution.getNewField());
			} else if (getEffortEstimatedField().equals(entry.getKey())) {
				return false;
			} 
		}				
		return true;
	}
	
}
