package com.pmease.commons.loader;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.pmease.commons.util.Dependency;

public abstract class AbstractPluginModule extends AbstractModule implements Dependency {

	private String pluginId;
	
	private String pluginVersion;
	
	private String pluginVendor;
	
	private String pluginName;
	
	private String pluginDescription;
	
	private Set<String> pluginDependencies = new HashSet<String>();

	@Override
	protected void configure() {
		final Class<? extends AbstractPlugin> pluginClass = getPluginClass();
		if (pluginClass != null) {
			Multibinder<AbstractPlugin> pluginBinder = Multibinder.newSetBinder(binder(), AbstractPlugin.class);
		    pluginBinder.addBinding().to(pluginClass).in(Singleton.class);
		    
		    bindListener(Matchers.any(), new TypeListener() {

				@Override
				public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
					if (pluginClass == type.getRawType()) {
						encounter.register(new InjectionListener<I>() {

							@Override
							public void afterInjection(I injectee) {
								AbstractPlugin plugin = (AbstractPlugin) injectee;
								plugin.setId(pluginId);
								plugin.setName(pluginName);
								plugin.setVendor(pluginVendor);
								plugin.setVersion(pluginVersion);
								plugin.setDescription(pluginDescription);
								plugin.setDependencyIds(pluginDependencies);
							}
							
						});
					}
				}
		    	
		    });
		}
	}

	protected abstract Class<? extends AbstractPlugin> getPluginClass();

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public void setPluginVendor(String pluginVendor) {
		this.pluginVendor = pluginVendor;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public void setPluginDescription(String pluginDescription) {
		this.pluginDescription = pluginDescription;
	}

	public void setPluginDependencies(Set<String> pluginDependencies) {
		this.pluginDependencies = pluginDependencies;
	}

	@Override
	public String getId() {
		return pluginId;
	}

	@Override
	public Set<String> getDependencyIds() {
		return pluginDependencies;
	}
	
}
