package com.pmease.commons.hibernate;

import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;

import com.google.inject.Provider;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.StringUtils;

@Singleton
public class ConfigurationProvider implements Provider<Configuration> {
	
	private Configuration configuration;
	
	private final Set<ModelProvider> modelProviders;
	
	private final NamingStrategy namingStrategy;
	
	private final Properties hibernateProperties;
	
	@Inject
	public ConfigurationProvider(Set<ModelProvider> modelProviders, NamingStrategy namingStrategy, 
			@Nullable @Named("hibernate") Properties hibernateProperties) {
		this.modelProviders = modelProviders;
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
			for (Class<? extends AbstractEntity> each: ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
				configuration.addAnnotatedClass(each);
			}
			
			for (ModelProvider provider: modelProviders) {
				for (Class<? extends AbstractEntity> modelClass: provider.getModelClasses())
					configuration.addAnnotatedClass(modelClass);
			}
			
			configuration.setProperties(hibernateProperties);	
		} 
		return configuration;
	}

}
