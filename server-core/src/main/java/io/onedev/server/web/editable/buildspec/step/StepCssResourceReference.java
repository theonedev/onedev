package io.onedev.server.web.editable.buildspec.step;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class StepCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public StepCssResourceReference() {
		super(StepCssResourceReference.class, "step.css");
	}

}
