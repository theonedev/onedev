package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ShowConditionInnerWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ShowCondition condition;
	
	@Editable(name="And")
	@NotNull
	public ShowCondition getCondition() {
		return condition;
	}

	public void setCondition(ShowCondition condition) {
		this.condition = condition;
	}
	
}
