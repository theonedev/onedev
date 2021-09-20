package io.onedev.server.git;

import java.io.Serializable;

public class LfsPointer implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String objectId;
	
	private final long objectSize;
	
	public LfsPointer(String objectId, long objectSize) {
		this.objectId = objectId;
		this.objectSize = objectSize;
	}

	public String getObjectId() {
		return objectId;
	}

	public long getObjectSize() {
		return objectSize;
	}

}
