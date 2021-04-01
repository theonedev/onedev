package io.onedev.server.buildspec.step;

import javax.annotation.Nullable;

public interface StepTemplateAware {
	
	@Nullable
	StepTemplate getTemplate();
	
}
