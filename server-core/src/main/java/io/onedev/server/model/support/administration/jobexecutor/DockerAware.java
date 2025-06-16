package io.onedev.server.model.support.administration.jobexecutor;

import io.onedev.k8shelper.RegistryLoginFacade;

import java.util.List;

public interface DockerAware {
	
	List<RegistryLoginFacade> getRegistryLogins(String jobToken);
	
}
