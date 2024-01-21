package io.onedev.server.entitymanager;

import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface PackBlobAuthorizationManager extends EntityManager<PackBlobAuthorization> {
	
	void authorize(Project project, PackBlob packBlob);
	
}
