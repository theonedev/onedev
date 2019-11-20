package io.onedev.server.util.script.identity;

import javax.annotation.Nullable;

public interface ScriptIdentityAware {

	@Nullable
	ScriptIdentity getScriptIdentity();
	
}
