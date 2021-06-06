package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class LabelMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String issueLabel;
	
	private String issueField;

	@Editable(order=100)
	@NotEmpty
	public String getIssueLabel() {
		return issueLabel;
	}

	public void setIssueLabel(String issueLabel) {
		this.issueLabel = issueLabel;
	}

	@Editable(order=200)
	@ChoiceProvider("getIssueFieldChoices")
	@NotEmpty
	public String getIssueField() {
		return issueField;
	}

	public void setIssueField(String issueField) {
		this.issueField = issueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs()) {
			if (field.getType().equals(InputSpec.ENUMERATION)) {
				for (String value: field.getPossibleValues()) 
					choices.add(field.getName() + " : " + value);
			}
		}
		return choices;
	}
	
}
