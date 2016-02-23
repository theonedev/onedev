package com.pmease.commons.hibernate;

import java.io.Serializable;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.type.Type;

import com.google.inject.Provider;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.StringUtils;

@Singleton
public class ConfigurationProvider implements Provider<Configuration> {
	
	private Configuration configuration;
	
	private final Set<ModelProvider> modelProviders;
	
	private final PhysicalNamingStrategy namingStrategy;
	
	private final Properties hibernateProperties;
	
	private final Set<HibernateListener> hibernateListeners;
	
	@Inject
	public ConfigurationProvider(Set<ModelProvider> modelProviders, PhysicalNamingStrategy namingStrategy, 
			@Nullable @Named("hibernate") Properties hibernateProperties, 
			Set<HibernateListener> hibernateListeners) {
		this.modelProviders = modelProviders;
		this.namingStrategy = namingStrategy;
		this.hibernateProperties = hibernateProperties;
		this.hibernateListeners = hibernateListeners;
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
			configuration.setPhysicalNamingStrategy(namingStrategy);
			for (Class<? extends AbstractEntity> each: ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
				configuration.addAnnotatedClass(each);
			}
			
			for (ModelProvider provider: modelProviders) {
				for (Class<? extends AbstractEntity> modelClass: provider.getModelClasses())
					configuration.addAnnotatedClass(modelClass);
			}
			
			configuration.setInterceptor(new EmptyInterceptor() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
						Type[] types) throws CallbackException {
					boolean changed = false;
					for (HibernateListener listener: hibernateListeners) {
						if (listener.onLoad(entity, id, state, propertyNames, types))
							changed = true;
					}
						
					return changed;
				}

				@Override
				public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
						Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
					boolean changed = false;
					for (HibernateListener listener: hibernateListeners) {
						if (listener.onFlushDirty(entity, id, currentState, previousState, propertyNames, types))
							changed = true;
					}
						
					return changed;
				}

				@Override
				public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
						Type[] types) throws CallbackException {
					boolean changed = false;
					for (HibernateListener listener: hibernateListeners) {
						if (listener.onSave(entity, id, state, propertyNames, types))
							changed = true;
					}
						
					return changed;
				}

				@Override
				public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
						Type[] types) throws CallbackException {
					for (HibernateListener listener: hibernateListeners)
						listener.onDelete(entity, id, state, propertyNames, types);
				}

			});
			configuration.setProperties(hibernateProperties);	
		} 
		return configuration;
	}

}
