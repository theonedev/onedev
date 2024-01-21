package io.onedev.server.entitymanager;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface PackBlobReferenceManager extends EntityManager<PackBlobReference> {

	void create(PackBlobReference blobReference);
	
	void createIfNotExist(Pack pack, PackBlob packBlob);
	
	@Nullable
	PackBlobReference findNotPendingDelete(PackBlob packBlob);
	
}
