package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.web.editable.annotation.Editable;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

@Editable
public class PerformanceSetting implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private static final Logger logger=  LoggerFactory.getLogger(PerformanceSetting.class);
	
	private int cpuIntensiveTaskConcurrency;
	
	private int maxGitLFSFileSize = 4096;  
	
	private int maxUploadFileSize = 20;
	
	private int maxCodeSearchEntries = 100;
	
	public PerformanceSetting() {
		try {
			HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
			cpuIntensiveTaskConcurrency = hardware.getProcessor().getLogicalProcessorCount();
		} catch (Exception e) {
			logger.debug("Error calling oshi", e);
			cpuIntensiveTaskConcurrency = 4;
		}
	}

	@Editable(order=100, name="CPU Intensive Task Concurrency", description="" +
			"Specify max number of CPU intensive tasks the server can run concurrently, " +
			"such as Git repository pull/push, repository index, etc.")
	public int getCpuIntensiveTaskConcurrency() {
		return cpuIntensiveTaskConcurrency;
	}

	public void setCpuIntensiveTaskConcurrency(int cpuIntensiveTaskConcurrency) {
		this.cpuIntensiveTaskConcurrency = cpuIntensiveTaskConcurrency;
	}

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
