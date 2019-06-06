package io.onedev.server.ci;

import javax.annotation.Nullable;

public interface CISpecAware {
	
	@Nullable
	CISpec getCISpec();
	
}
