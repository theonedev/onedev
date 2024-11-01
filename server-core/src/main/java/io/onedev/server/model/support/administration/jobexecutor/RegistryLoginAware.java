package io.onedev.server.model.support.administration.jobexecutor;

import io.onedev.k8shelper.RegistryLoginFacade;

import java.util.List;

public interface RegistryLoginAware {
	
	List<RegistryLoginFacade> getRegistryLogins(String jobToken);

}
