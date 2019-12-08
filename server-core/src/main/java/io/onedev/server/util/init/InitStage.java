package io.onedev.server.util.init;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class InitStage implements Serializable, Cloneable {
	
	private String message;
	
	private final List<ManualConfig> manualConfigs;
	
	public InitStage(String message, List<ManualConfig> manualConfigs) {
		this.message = message;
		this.manualConfigs = manualConfigs;
	}
	
	public InitStage(String message) {
		this(message, new ArrayList<ManualConfig>());
	}
	
	public String getMessage() {
		return message;
	}
	
	public List<ManualConfig> getManualConfigs() {
		return manualConfigs;
	}
	
	public synchronized void waitForFinish() {
		if (!manualConfigs.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public synchronized void finished() {
		message = "Please wait...";
		manualConfigs.clear();
		notify();
	}
	
}
