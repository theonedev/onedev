package io.onedev.server.model.support.administration.groovyscript;

import java.io.Serializable;

import io.onedev.server.util.scriptidentity.ScriptIdentity;

public interface ScriptAuthorization extends Serializable {

	boolean isAuthorized(ScriptIdentity identity);
	
}
