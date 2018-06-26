package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Condition")
public class ShowConditionOuterWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ShowConditionInnerWrapper innerWrapper = new ShowConditionInnerWrapper();

	@Editable
	@NotNull
	public ShowConditionInnerWrapper getInnerWrapper() {
		return innerWrapper;
	}

	public void setInnerWrapper(ShowConditionInnerWrapper innerWrapper) {
		this.innerWrapper = innerWrapper;
	}
	
}
