package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.dao.EntityManager;

public interface LinkSpecManager extends EntityManager<LinkSpec> {

	@Nullable
	LinkSpec find(String name);

}
