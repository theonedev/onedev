package io.onedev.server.util.init;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class InitStage implements Serializable, Cloneable {
	
	private final String message;
	
	private final List<ManualConfig> manualConfigs;
	
	public InitStage(String message, List<ManualConfig> manualConfigs) {
		this.message = message;
		this.manualConfigs = manualConfigs;
	}
	
	public InitStage(String message) {
		this(message, new ArrayList<>());
	}
	
	public String getMessage() {
		return message;
	}
	
	public List<ManualConfig> getManualConfigs() {
		return manualConfigs;
	}
	
}
