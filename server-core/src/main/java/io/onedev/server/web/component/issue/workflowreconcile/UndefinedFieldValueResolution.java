package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowReconcilePanel.UndefinedFieldValueContainer;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable
public class UndefinedFieldValueResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_VALUE, DELETE_THIS_VALUE}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_VALUE;
	
	private String newValue;
	
	@Editable(order=50)
	@OmitName
	@NotNull
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100)
	@ChoiceProvider("getValueChoices")
	@ShowCondition("isNewValueVisible")
	@OmitName
	@NotEmpty
	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@SuppressWarnings("unused")
	private static boolean isNewValueVisible() {
		return EditContext.get().getInputValue("fixType") == FixType.CHANGE_TO_ANOTHER_VALUE;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		UndefinedFieldValueContainer container = ComponentContext.get().getComponent()
				.findParent(UndefinedFieldValueContainer.class); 
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		FieldSpec fieldSpec = Preconditions.checkNotNull(issueSetting.getFieldSpec(container.getFieldName()));
		ComponentContext.push(new ComponentContext(container));
		try {
			return new ArrayList<>(((ChoiceField)fieldSpec).getChoiceProvider().getChoices(true).keySet());
		} finally {
			ComponentContext.pop();
		}
	}

}
