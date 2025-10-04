package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.LinkAuthorization;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;

public interface LinkAuthorizationService extends EntityService<LinkAuthorization> {

	void syncAuthorizations(Role role, Collection<LinkSpec> authorizedLinks);

    void create(LinkAuthorization authorization);

}
