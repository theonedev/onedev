package io.onedev.server.web.editable.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class StepEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Step step;

	@Editable
	@NotNull
	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}
	
}
