package com.pmease.commons.persistence;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;

import com.google.inject.Provider;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.persistence.extensionpoints.ModelContribution;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.StringUtils;

@Singleton
public class ConfigurationProvider implements Provider<Configuration> {
	
	private Configuration configuration;
	
	private final PluginManager pluginManager;
	
	private final NamingStrategy namingStrategy;
	
	private final Properties hibernateProperties;
	
	@Inject
	public ConfigurationProvider(PluginManager pluginManager, NamingStrategy namingStrategy, 
			@Nullable @Hibernate Properties hibernateProperties) {
		this.pluginManager = pluginManager;
		this.namingStrategy = namingStrategy;
		this.hibernateProperties = hibernateProperties;
	}
	
	@Override
	public synchronized Configuration get() {
		if (hibernateProperties == null)
			return null;
		
		if (configuration == null) {
			String url = hibernateProperties.getProperty(Environment.URL);
			hibernateProperties.setProperty(Environment.URL, 
					StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
			
			configuration = new Configuration();
			configuration.setNamingStrategy(namingStrategy);
			Collection<Class<AbstractEntity>> modelClasses = 
					ClassUtils.findSubClasses(AbstractEntity.class, AbstractEntity.class);
			for (Class<AbstractEntity> model: modelClasses) {
				if (!Modifier.isAbstract(model.getModifiers()))
					configuration.addAnnotatedClass(model);
			}
			
			Collection<ModelContribution> contributions = 
					pluginManager.getExtensions(ModelContribution.class);
			for (ModelContribution contribution: contributions) {
				for (Class<? extends AbstractEntity> modelClass: contribution.getModelClasses())
					configuration.addAnnotatedClass(modelClass);
			}
			
			configuration.setProperties(hibernateProperties);	
		} 
		return configuration;
	}

}
