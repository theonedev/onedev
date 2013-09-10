package com.pmease.commons.loader;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.dependency.Dependency;

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
			addExtension(AbstractPlugin.class, pluginClass);
		    
		    bindListener(new AbstractMatcher<TypeLiteral<?>>() {

				@Override
				public boolean matches(TypeLiteral<?> t) {
					return t.getRawType() == pluginClass;
				}
		    	
		    }, new TypeListener() {

				@Override
				public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
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
	
	protected <T> void addExtension(Class<T> extensionPoint, Class<? extends T> extensionClass) {
		Multibinder<T> pluginBinder = Multibinder.newSetBinder(binder(), extensionPoint);
	    pluginBinder.addBinding().to(extensionClass).in(Singleton.class);
	}
	
	protected <T> void addExtensionsFromPackage(Class<T> extensionPoint, Class<?> packageLocator) {
		for (Class<? extends T> subClass: ClassUtils.findSubClasses(extensionPoint, packageLocator)) {
			if (ClassUtils.isConcrete(subClass))
				addExtension(extensionPoint, subClass);
		}
	}
		
}
