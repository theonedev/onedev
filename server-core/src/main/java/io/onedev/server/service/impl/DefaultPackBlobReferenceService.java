package io.onedev.server.service.impl;

import static io.onedev.server.model.PackBlobReference.PROP_PACK;
import static io.onedev.server.model.PackBlobReference.PROP_PACK_BLOB;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PackBlobReferenceService;

@Singleton
public class DefaultPackBlobReferenceService extends BaseEntityService<PackBlobReference>
		implements PackBlobReferenceService, Serializable {

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
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobReferenceService.class);
	}
	
}
