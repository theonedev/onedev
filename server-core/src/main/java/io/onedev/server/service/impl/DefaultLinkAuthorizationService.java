package io.onedev.server.service.impl;

import java.util.Collection;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.LinkAuthorization;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.LinkAuthorizationService;

@Singleton
public class DefaultLinkAuthorizationService extends BaseEntityService<LinkAuthorization> implements LinkAuthorizationService {

	@Transactional
	@Override
	public void syncAuthorizations(Role role, Collection<LinkSpec> authorizedLinks) {
		for (LinkAuthorization authorization: role.getLinkAuthorizations()) {
			if (!authorizedLinks.contains(authorization.getLink()))
				delete(authorization);
		}

		for (LinkSpec link: authorizedLinks) {
			boolean found = false;
			for (LinkAuthorization authorization: role.getLinkAuthorizations()) {
				if (authorization.getLink().equals(link)) {
					found = true;
					break;
				}
			}
			if (!found) {
				LinkAuthorization authorization = new LinkAuthorization();
				authorization.setLink(link);
				authorization.setRole(role);
				create(authorization);
			}
		}
	}

	@Transactional
	@Override
	public void create(LinkAuthorization authorization) {
		Preconditions.checkState(authorization.isNew());
		dao.persist(authorization);
	}

}
