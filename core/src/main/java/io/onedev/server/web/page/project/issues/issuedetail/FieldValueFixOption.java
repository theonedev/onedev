package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.ShowCondition;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.multichoiceinput.MultiChoiceInput;
import io.onedev.server.web.util.ComponentContext;

@Editable
public class FieldValueFixOption implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_VALUE, DELETE_THIS_VALUE}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_VALUE;
	
	private String newValue;
	
	private boolean fixAll;

	@Editable(order=50)
	@NotNull
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100, name="Map to Value", description="Select existing value to map the invalid value to")
	@ChoiceProvider("getValueChoices")
	@ShowCondition("isNewValueVisible")
	@NotEmpty
	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@Editable(order=200, description="Enable to fix the problem for all issues in the project, "
			+ "otherwise only fix for current issue")
	public boolean isFixAll() {
		return fixAll;
	}

	public void setFixAll(boolean fixAll) {
		this.fixAll = fixAll;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNewValueVisible() {
		return OneContext.get().getEditContext().getInputValue("fixType") == FixType.CHANGE_TO_ANOTHER_VALUE;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		FieldValueFixPanel fieldValueFixPanel = ((ComponentContext)OneContext.get()).getComponent().findParent(FieldValueFixPanel.class); 
		OneContext.push(new ComponentContext(fieldValueFixPanel));
		try {
			MultiValueIssueField fixField = fieldValueFixPanel.getField();
			InputSpec fieldSpec = Preconditions.checkNotNull(OneContext.get().getProject().getIssueWorkflow().getField(fixField.getName()));
			if (fieldSpec instanceof ChoiceInput)
				return ((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(false);
			else
				return ((MultiChoiceInput)fieldSpec).getChoiceProvider().getChoices(false);
		} finally {
			OneContext.pop();
		}
	}

}
