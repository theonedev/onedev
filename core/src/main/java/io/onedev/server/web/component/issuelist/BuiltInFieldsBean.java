package io.onedev.server.web.component.issuelist;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BuiltInFieldsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String milestone;

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
	private static List<String> getMilestoneChoices() {
		return OneContext.get().getProject().getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
	}
}
