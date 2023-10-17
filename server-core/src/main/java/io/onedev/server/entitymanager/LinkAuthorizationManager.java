package io.onedev.server.entitymanager;

import io.onedev.server.model.LinkAuthorization;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface LinkAuthorizationManager extends EntityManager<LinkAuthorization> {

	void syncAuthorizations(Role role, Collection<LinkSpec> authorizedLinks);

    void create(LinkAuthorization authorization);
}
