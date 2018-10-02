package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BuiltInFieldsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String state;
	
	private String milestone;

	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Editable(order=200)
	@ChoiceProvider("getMilestoneChoices")
	@NameOfEmptyValue("No milestone")
	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		return OneContext.get().getProject().getIssueWorkflow().getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unused")
	private static List<String> getMilestoneChoices() {
		return OneContext.get().getProject().getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
