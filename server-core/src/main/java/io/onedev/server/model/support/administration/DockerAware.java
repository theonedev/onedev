package io.onedev.server.model.support.administration;

import io.onedev.k8shelper.RegistryLoginFacade;

import java.util.List;

public interface DockerAware {
	
	List<RegistryLoginFacade> getRegistryLogins(String token);
	
}
