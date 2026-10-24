package io.onedev.server.web.asset.pipelinebutton;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class PipelineButtonCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PipelineButtonCssResourceReference() {
		super(PipelineButtonCssResourceReference.class, "pipeline-button.css");
	}

}
