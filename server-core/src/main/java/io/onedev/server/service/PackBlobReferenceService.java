package io.onedev.server.service;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;

public interface PackBlobReferenceService extends EntityService<PackBlobReference> {

	void create(PackBlobReference blobReference);
	
	void createIfNotExist(Pack pack, PackBlob packBlob);
		
}
