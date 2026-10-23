package io.onedev.server.model.support.administration;

import java.util.List;

import io.onedev.k8shelper.RegistryLoginFacade;

public interface RegistryLoginAware {

	List<RegistryLoginFacade> getRegistryLogins(String token);

}
