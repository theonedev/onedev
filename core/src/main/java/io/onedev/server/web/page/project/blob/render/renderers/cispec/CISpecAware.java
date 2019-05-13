package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import javax.annotation.Nullable;

import io.onedev.server.ci.CISpec;

public interface CISpecAware {
	
	@Nullable
	CISpec getCISpec();
	
}
