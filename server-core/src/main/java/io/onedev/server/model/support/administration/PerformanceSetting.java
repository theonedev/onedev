package io.onedev.server.model.support.administration;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class PerformanceSetting implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private int maxGitLFSFileSize = 4096;  
	
	private int maxUploadFileSize = 20;
	
	private int maxCodeSearchEntries = 100;
	
	@Editable(order=600, name="Max Git LFS File Size (MB)", description="Specify max git LFS file size in mega bytes")
	public int getMaxGitLFSFileSize() {
		return maxGitLFSFileSize;
	}

	public void setMaxGitLFSFileSize(int maxGitLFSFileSize) {
		this.maxGitLFSFileSize = maxGitLFSFileSize;
	}

	@Editable(order=700, name="Max Upload File Size (MB)", description="Specify max size of uploaded file in mega bytes. "
			+ "This applies to file uploaded to repository via web interface, as well as file uploaded to markdown content "
			+ "(issue comment etc)")
	public int getMaxUploadFileSize() {
		return maxUploadFileSize;
	}

	public void setMaxUploadFileSize(int maxUploadFileSize) {
		this.maxUploadFileSize = maxUploadFileSize;
	}

	@Editable(order=800, description="Maximum number of entries to return when search code in repository")
	public int getMaxCodeSearchEntries() {
		return maxCodeSearchEntries;
	}

	public void setMaxCodeSearchEntries(int maxCodeSearchEntries) {
		this.maxCodeSearchEntries = maxCodeSearchEntries;
	}

}
