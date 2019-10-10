package io.onedev.server.util.scriptidentity;

import javax.annotation.Nullable;

public interface ScriptIdentityAware {

	@Nullable
	ScriptIdentity getScriptIdentity();
	
}
