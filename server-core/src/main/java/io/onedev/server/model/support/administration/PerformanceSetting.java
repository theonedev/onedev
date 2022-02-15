package io.onedev.server.model.support.administration;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

@Editable
public class PerformanceSetting implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private int cpuIntensiveTaskConcurrency;
	
	private int serverJobExecutorCpuQuota;
	
	private int serverJobExecutorMemoryQuota;
	
	private int maxGitLFSFileSize = 4096;  
	
	private int maxUploadFileSize = 20;
	
	private int maxCodeSearchEntries = 100;
	
	public PerformanceSetting() {
		HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
		cpuIntensiveTaskConcurrency = hardware.getProcessor().getLogicalProcessorCount();
		serverJobExecutorCpuQuota = hardware.getProcessor().getLogicalProcessorCount()*1000;
		serverJobExecutorMemoryQuota = (int) (hardware.getMemory().getTotal()/1024/1024);				
	}

	@Editable(order=100, name="CPU Intensive Task Concurrency", description="Specify max concurrent CPU intensive "
			+ "tasks, such as Git repository pull/push, repository index, etc.")
	public int getCpuIntensiveTaskConcurrency() {
		return cpuIntensiveTaskConcurrency;
	}

	public void setCpuIntensiveTaskConcurrency(int cpuIntensiveTaskConcurrency) {
		this.cpuIntensiveTaskConcurrency = cpuIntensiveTaskConcurrency;
	}

	@Editable(order=400, name="Server Job Executor CPU Quota", description="Specify CPU quota to run build jobs "
			+ "on server (via server docker/shell executor). This is normally <i>(CPU cores)*1000</i>, for "
			+ "instance <i>4000</i> means 4 CPU cores. CPU requirements of all build jobs running on server "
			+ "will be limited by this quota")
	public int getServerJobExecutorCpuQuota() {
		return serverJobExecutorCpuQuota;
	}

	public void setServerJobExecutorCpuQuota(int serverJobExecutorCpuQuota) {
		this.serverJobExecutorCpuQuota = serverJobExecutorCpuQuota;
	}

	@Editable(order=500, name="Server Job Executor Memory Quota", description="Specify memory quota in mega bytes "
			+ "to run builds jobs on server (via server docker/shell executor). Memory requirements of all build "
			+ "jobs running on server will be limited by this quota")
	public int getServerJobExecutorMemoryQuota() {
		return serverJobExecutorMemoryQuota;
	}

	public void setServerJobExecutorMemoryQuota(int serverJobExecutorMemoryQuota) {
		this.serverJobExecutorMemoryQuota = serverJobExecutorMemoryQuota;
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
