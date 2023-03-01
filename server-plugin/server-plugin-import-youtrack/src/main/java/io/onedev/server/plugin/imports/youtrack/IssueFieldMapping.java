package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class IssueFieldMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String youTrackIssueField;
	
	private String oneDevIssueField;

	@Editable(order=100, name="YouTrack Issue Field")
	@NotEmpty
	public String getYouTrackIssueField() {
		return youTrackIssueField;
	}

	public void setYouTrackIssueField(String youTrackIssueField) {
		this.youTrackIssueField = youTrackIssueField;
	}

	@Editable(order=200, name="OneDev Issue Field")
	@ChoiceProvider("getOneDevIssueFieldChoices")
	@NotEmpty
	public String getOneDevIssueField() {
		return oneDevIssueField;
	}

	public void setOneDevIssueField(String oneDevIssueField) {
		this.oneDevIssueField = oneDevIssueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getOneDevIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs()) {
			if (field.getType().equals(InputSpec.ENUMERATION)) {
				for (String value: field.getPossibleValues()) 
					choices.add(field.getName() + "::" + value);
			} else {
				choices.add(field.getName());
			}
		}
		return choices;
	}
	
}
