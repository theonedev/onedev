package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class TransitionPrerequisite implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	private List<String> fieldValues;

	@Editable(order=100, name="When field")
	@ChoiceProvider("getFieldNameChoices")
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Editable(order=200, name="Is set to")
	@ChoiceProvider("getFieldValueChoices")
	public List<String> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(List<String> fieldValues) {
		this.fieldValues = fieldValues;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldNameChoices() {
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		return page.getWorkflow().getInputNames();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldValueChoices() {
		String fieldName = (String) OneContext.get().getEditContext().getOnScreenValue("fieldName");
		if (fieldName != null) {
			IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
			return page.getWorkflow().getInput(fieldName).getPossibleValues();
		} else {
			return new ArrayList<>();
		}
	}
	
}
