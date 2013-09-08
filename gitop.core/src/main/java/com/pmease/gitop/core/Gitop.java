package com.pmease.gitop.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.extensionpoints.ModelContribution;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitop.core.manager.InitManager;
import com.pmease.gitop.core.model.ModelLocator;

public class Gitop extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Gitop.class);
	
	private final InitManager initManager;
	
	private List<ManualConfig> manualConfigs;
	
	public Gitop(InitManager initManager) {
		this.initManager = initManager;
	}
	
	@Override
	public Collection<?> getExtensions() {
		return Arrays.asList(
				new ModelContribution() {
			
					@Override
					public Collection<Class<? extends AbstractEntity>> getModelClasses() {
						Collection<Class<? extends AbstractEntity>> modelClasses = 
								new HashSet<Class<? extends AbstractEntity>>();
						modelClasses.addAll(ClassUtils.findSubClasses(AbstractEntity.class, ModelLocator.class));
						return modelClasses;
					}
				}
			);
	}

	@SuppressWarnings("serial")
	@Override
	public void start() {
		super.start();
		
		manualConfigs = initManager.init();

		if (!manualConfigs.isEmpty()) synchronized (manualConfigs) {

			final ManualConfig lastConfig = manualConfigs.remove(manualConfigs.size()-1);
			
			manualConfigs.add(new ManualConfig(lastConfig.getSetting()) {

				@Override
				public Skippable getSkippable() {
					return lastConfig.getSkippable();
				}

				@Override
				public void complete() {
					lastConfig.complete();
					synchronized (manualConfigs) {
						manualConfigs.notify();
					}
				}
				
			});
			
			logger.warn("Please point your browser to '" + guessServerUrl() + "' to set up the server.");
			
			try {
				manualConfigs.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			manualConfigs.clear();
		}
	}
	
	private String guessServerUrl() {
		return "http://localhost:8080";
	}
	
	/**
	 * This method can be called from different UI threads, so we clone manual configs to 
	 * make it thread-safe.
	 * <p>
	 * @return
	 * 			cloned list of manual configs
	 */
	@SuppressWarnings("unchecked")
	public List<ManualConfig> getManualConfigs() {
		synchronized (manualConfigs) {
			return (List<ManualConfig>) SerializationUtils.clone((Serializable) manualConfigs);
		}
	}
	
	public static Gitop getInstance() {
		return AppLoader.getInstance(Gitop.class);
	}
	
	public static <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}

}
