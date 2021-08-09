package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GlobalBuildSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedBuildQuery> namedQueries = new ArrayList<>();
	
	private List<String> listParams = new ArrayList<>();
	
	private int cpu;
	
	private int memory;
	
	@SuppressWarnings("restriction")
	public GlobalBuildSetting() {
		namedQueries.add(new NamedBuildQuery("All", null));
		namedQueries.add(new NamedBuildQuery("Successful", "successful"));
		namedQueries.add(new NamedBuildQuery("Failed", "failed"));
		namedQueries.add(new NamedBuildQuery("Cancelled", "cancelled"));
		namedQueries.add(new NamedBuildQuery("Timed out", "timed out"));
		namedQueries.add(new NamedBuildQuery("Running", "running"));
		namedQueries.add(new NamedBuildQuery("Waiting", "waiting"));
		namedQueries.add(new NamedBuildQuery("Pending", "pending"));
		namedQueries.add(new NamedBuildQuery("Build recently", "\"Submit Date\" is since \"last week\""));
		
		cpu = Runtime.getRuntime().availableProcessors()*1000;
		
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
			     java.lang.management.ManagementFactory.getOperatingSystemMXBean();
		memory = (int)(os.getTotalPhysicalMemorySize()/1024/1024);				
	}
	
	public List<NamedBuildQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedBuildQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	public List<String> getListParams() {
		return listParams;
	}

	public void setListParams(List<String> listParams) {
		this.listParams = listParams;
	}

	@Editable(order=100, name="Server CPU", description="Specify CPU capability in millis available to run builds "
			+ "on server (via server docker/shell executor). This is normally <i>(CPU cores)*1000</i>, for "
			+ "instance <i>4000</i> means 4 CPU cores")
	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	@Editable(order=200, name="Server Memory", description="Specify physical memory in mega bytes available to run "
			+ "builds on server (via server docker/shell executor)")
	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	@Nullable
	public NamedBuildQuery getNamedQuery(String name) {
		for (NamedBuildQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
