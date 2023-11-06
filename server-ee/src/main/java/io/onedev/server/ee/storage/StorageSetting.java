package io.onedev.server.ee.storage;

import io.onedev.server.annotation.DataDirectory;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Editable
public class StorageSetting implements ContributedAdministrationSetting {

	private static final long serialVersionUID = 1L;

	private String lfsStore;
	
	private String artifactStore;
	
	private String packStore;

	@Editable(order=100, name="Git LFS Storage", description = "Optionally specify separate directory to store git lfs files. " +
			"Non-absolute directory is considered to be relative to site directory")
	@DataDirectory
	public String getLfsStore() {
		return lfsStore;
	}

	public void setLfsStore(String lfsStore) {
		this.lfsStore = lfsStore;
	}

	@Editable(order=200, name="Build Artifact Storage", description = "Optionally specify separate directory to store build artifacts. " +
			"Non-absolute directory is considered to be relative to site directory")
	@DataDirectory
	public String getArtifactStore() {
		return artifactStore;
	}

	public void setArtifactStore(String artifactStore) {
		this.artifactStore = artifactStore;
	}

	@Editable(order=300, name="Package Storage", description = "Optionally specify separate directory to store package files. " +
			"Non-absolute directory is considered to be relative to site directory")
	@DataDirectory
	public String getPackStore() {
		return packStore;
	}

	public void setPackStore(String packStore) {
		this.packStore = packStore;
	}
}
