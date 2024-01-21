package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.entitymanager.PackBlobReferenceManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static io.onedev.server.model.Pack.PROP_PROJECT;
import static io.onedev.server.model.PackBlobReference.PROP_PACK;
import static io.onedev.server.model.PackBlobReference.PROP_PACK_BLOB;
import static io.onedev.server.model.Project.PROP_PENDING_DELETE;

@Singleton
public class DefaultPackBlobReferenceManager extends BaseEntityManager<PackBlobReference> 
		implements PackBlobReferenceManager, Serializable {

	@Inject
	public DefaultPackBlobReferenceManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void create(PackBlobReference blobReference) {
		Preconditions.checkState(blobReference.isNew());
		dao.persist(blobReference);
	}

	@Transactional
	@Override
	public void createIfNotExist(Pack pack, PackBlob packBlob) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PACK, pack));
		criteria.add(Restrictions.eq(PROP_PACK_BLOB, packBlob));
		if (find(criteria) == null) {
			var blobReference = new PackBlobReference();
			blobReference.setPack(pack);
			blobReference.setPackBlob(packBlob);
			dao.persist(blobReference);
		}
	}
	
	@Override
	public PackBlobReference findNotPendingDelete(PackBlob packBlob) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PACK_BLOB, packBlob));
		criteria.createCriteria(PROP_PACK).createCriteria(PROP_PROJECT).add(Restrictions.eq(PROP_PENDING_DELETE, false));
		return find(criteria);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobReferenceManager.class);
	}
	
}
