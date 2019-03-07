package io.onedev.server.web.page.project.issueworkflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issueworkflowreconcile.WorkflowReconcilePanel.UndefinedFieldValueContainer;
import io.onedev.server.web.util.WicketUtils;

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
		return OneContext.get().getEditContext().getInputValue("fixType") == FixType.CHANGE_TO_ANOTHER_VALUE;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		UndefinedFieldValueContainer container = OneContext.get().getComponent()
				.findParent(UndefinedFieldValueContainer.class); 
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		InputSpec fieldSpec = Preconditions.checkNotNull(issueSetting.getFieldSpec(container.getFieldName()));
		OneContext.push(new OneContext(container) {

			@Override
			public Project getProject() {
				ProjectPage page = (ProjectPage) WicketUtils.getPage();
				return page.getProject();
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}

		});
		try {
			return new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
		} finally {
			OneContext.pop();
		}
	}

}
