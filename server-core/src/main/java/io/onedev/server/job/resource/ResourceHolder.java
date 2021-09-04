package io.onedev.server.job.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceHolder {

	public static final String CPU = "cpu";
	
	public static final String MEMORY = "memory";
	
	private final Map<String, Integer> totalResources;
	
	private final Map<String, Integer> usedResources = new HashMap<>();

	public ResourceHolder(Map<String, Integer> totalResources) {
		this.totalResources = totalResources;
	}
	
	public int getSpareResources(Map<String, Integer> resourceRequirements) {
		for (Map.Entry<String, Integer> entry: resourceRequirements.entrySet()) {
			Integer totalCount = totalResources.get(entry.getKey());
			if (totalCount == null)
				totalCount = 0;
			Integer usedCount = usedResources.get(entry.getKey());
			if (usedCount == null)
				usedCount = 0;
			if (usedCount + entry.getValue() > totalCount)
				return 0;
		}
		
		Integer cpuTotal = totalResources.get(CPU);
		if (cpuTotal == null)
			cpuTotal = 0;
		Integer memoryTotal = totalResources.get(MEMORY);
		if (memoryTotal == null)
			memoryTotal = 0;
		
		Integer cpuUsed = usedResources.get(CPU);
		if (cpuUsed == null)
			cpuUsed = 0;
		Integer cpuRequired = resourceRequirements.get(CPU);
		if (cpuRequired == null)
			cpuRequired = 0;
		cpuUsed += cpuRequired;
		if (cpuUsed == 0)
			cpuUsed = 1;
		
		Integer memoryUsed = usedResources.get(CPU);
		if (memoryUsed == null)
			memoryUsed = 0;
		Integer memoryRequired = resourceRequirements.get(CPU);
		if (memoryRequired == null)
			memoryRequired = 0;
		memoryUsed += memoryRequired;
		if (memoryUsed == 0)
			memoryUsed = 1;
		
		int spare = cpuTotal*400/cpuUsed + memoryTotal*100/memoryUsed;
		if (spare <= 0)
			spare = 1;
		return spare;
	}
	
	public void acquireResources(Map<String, Integer> resourceRequirements) {
		for (Map.Entry<String, Integer> entry: resourceRequirements.entrySet()) {
			Integer usedCount = usedResources.get(entry.getKey());
			if (usedCount == null)
				usedCount = 0;
			usedResources.put(entry.getKey(), usedCount + entry.getValue());
		}
	}
	
	public void releaseResources(Map<String, Integer> resourceRequirements) {
		for (Map.Entry<String, Integer> entry: resourceRequirements.entrySet()) {
			Integer usedCount = usedResources.get(entry.getKey());
			if (usedCount == null)
				usedCount = 0;
			usedResources.put(entry.getKey(), usedCount - entry.getValue());
		}
	}
	
	public void updateTotalResource(String name, int count) {
		totalResources.put(name, count);
	}
	
	public boolean hasUsedResources() {
		for (Integer usedCount: usedResources.values()) {
			if (usedCount != 0)
				return true;
		}
		return false;
	}
	
}
