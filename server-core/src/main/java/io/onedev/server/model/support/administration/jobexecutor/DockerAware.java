package io.onedev.server.model.support.administration.jobexecutor;

import java.util.List;

public interface DockerAware {
	
	List<RegistryLogin> getRegistryLogins();
	
}
