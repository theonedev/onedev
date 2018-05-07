package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class TransitionPrerequisite implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	private ValueSpecification valueSpecification;
	
	@Editable(order=100, name="When field")
	@ChoiceProvider("getFieldNameChoices")
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Editable(order=200)
	@NotNull
	@OmitName
	public ValueSpecification getValueSpecification() {
		return valueSpecification;
	}

	public void setValueSpecification(ValueSpecification valueSpecification) {
		this.valueSpecification = valueSpecification;
	}

	@SuppressWarnings("unused")
	private static List<String> getFieldNameChoices() {
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		return page.getWorkflow().getInputNames();
	}
	
}
