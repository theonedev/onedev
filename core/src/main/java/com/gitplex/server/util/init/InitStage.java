package com.gitplex.server.util.init;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

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
	
	public synchronized void waitFor() {
		if (!manualConfigs.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public synchronized InitStage clone() {
		if (!manualConfigs.isEmpty()) {
			List<ManualConfig> clonedConfigs = new ArrayList<ManualConfig>();
			for (ManualConfig each: manualConfigs)
				clonedConfigs.add(SerializationUtils.clone(each));
			
			final ManualConfig lastConfig = clonedConfigs.remove(clonedConfigs.size()-1);
			
			clonedConfigs.add(new ManualConfig(lastConfig.getMessage(), lastConfig.getSetting()) {
	
				@Override
				public Skippable getSkippable() {
					final Skippable skippable = lastConfig.getSkippable();
					if (skippable != null) {
						return new Skippable() {

							@Override
							public void skip() {
								skippable.skip();
								synchronized (InitStage.this) {
									message = "Please wait...";
									InitStage.this.manualConfigs.clear();
									InitStage.this.notify();
								}
							}
							
						};
					} else {
						return null;
					}
				}
	
				@Override
				public void complete() {
					lastConfig.complete();
					synchronized (InitStage.this) {
						message = "Please wait...";
						InitStage.this.manualConfigs.clear();
						InitStage.this.notify();
					}
				}
				
			});
			
			return new InitStage(message, clonedConfigs);
		} else {
			return new InitStage(message);
		}
	}
}
