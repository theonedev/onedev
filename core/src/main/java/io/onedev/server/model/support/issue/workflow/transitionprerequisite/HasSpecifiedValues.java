package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=300, name="Is set to")
public class HasSpecifiedValues implements ValueSpecification {

	private static final long serialVersionUID = 1L;
	
	private List<String> fieldValues;
	
	@Override
	public boolean matches(List<String> values) {
		return values.containsAll(fieldValues);
	}

	@Editable(order=100, name="Is set to")
	@ChoiceProvider("getFieldValueChoices")
	@OmitName
	public List<String> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(List<String> fieldValues) {
		this.fieldValues = fieldValues;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldValueChoices() {
		String fieldName = (String) OneContext.get().getEditContext(1).getInputValue("fieldName");
		if (fieldName != null) {
			IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
			return page.getWorkflow().getInput(fieldName).getPossibleValues();
		} else {
			return new ArrayList<>();
		}
	}

}
