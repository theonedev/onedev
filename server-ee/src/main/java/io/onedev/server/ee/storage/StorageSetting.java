package io.onedev.server.ee.storage;

import io.onedev.server.annotation.DataDirectory;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Editable
public class StorageSetting implements ContributedAdministrationSetting {

	private static final long serialVersionUID = 1L;

	private String lfsStore;
	
	private String artifactStore;

	@Editable(order=100, description = "Optionally specify a directory to store git lfs files. " +
			"Non-absolute directory is considered to be relative to site directory. Leave " +
			"empty to store under directory '&lt;project dir&gt;/git/lfs'. ")
	@DataDirectory
	public String getLfsStore() {
		return lfsStore;
	}

	public void setLfsStore(String lfsStore) {
		this.lfsStore = lfsStore;
	}

	@Editable(order=200, description = "Optionally specify a directory to store build artifacts. " +
			"Non-absolute directory is considered to be relative to site directory. Leave empty " +
			"to store under directory '&lt;build dir&gt;/artifacts'")
	@DataDirectory
	public String getArtifactStore() {
		return artifactStore;
	}

	public void setArtifactStore(String artifactStore) {
		this.artifactStore = artifactStore;
	}
	
}
