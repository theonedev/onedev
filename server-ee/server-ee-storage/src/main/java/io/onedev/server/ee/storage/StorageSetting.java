package io.onedev.server.ee.storage;

import io.onedev.server.annotation.Editable;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Editable
public class StorageSetting implements ContributedAdministrationSetting {

	private static final long serialVersionUID = 1L;

	private String lfsStore;
	
	private String artifactStore;

	@Editable(order=100)
	public String getLfsStore() {
		return lfsStore;
	}

	public void setLfsStore(String lfsStore) {
		this.lfsStore = lfsStore;
	}

	@Editable(order=200)
	public String getArtifactStore() {
		return artifactStore;
	}

	public void setArtifactStore(String artifactStore) {
		this.artifactStore = artifactStore;
	}
	
}
