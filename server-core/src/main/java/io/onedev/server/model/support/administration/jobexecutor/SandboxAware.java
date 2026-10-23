package io.onedev.server.model.support.administration.jobexecutor;

import io.onedev.server.model.support.administration.RegistryLoginAware;

/** Runs both shell and Docker steps inside an isolated sandbox. */
public interface SandboxAware extends RegistryLoginAware {
}
