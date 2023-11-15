package io.onedev.server.entitymanager.impl;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.entitymanager.PackBlobAuthorizationManager;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static io.onedev.server.model.PackBlobAuthorization.PROP_PACK_BLOB;
import static io.onedev.server.model.PackBlobAuthorization.PROP_PROJECT;

@Singleton
public class DefaultPackBlobAuthorizationManager extends BaseEntityManager<PackBlobAuthorization> 
		implements PackBlobAuthorizationManager, Serializable {

	@Inject
	public DefaultPackBlobAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void authorize(Project project, PackBlob packBlob) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_PACK_BLOB, packBlob));
		if (find(criteria) == null) {
			var authorization = new PackBlobAuthorization();
			authorization.setProject(project);
			authorization.setPackBlob(packBlob);
			dao.persist(authorization);
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobAuthorizationManager.class);
	}
	
}
