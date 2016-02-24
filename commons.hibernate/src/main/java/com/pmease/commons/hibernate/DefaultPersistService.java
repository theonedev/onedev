package com.pmease.commons.hibernate;

import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.util.ClassUtils;

@Singleton
public class DefaultPersistService implements PersistService, Provider<SessionFactory> {

	private final Set<ModelProvider> modelProviders;

	private final PhysicalNamingStrategy physicalNamingStrategy;

	private final Properties properties;
	
	private final Interceptor interceptor;
	
	private volatile SessionFactory sessionFactory;
	
	@Inject
	public DefaultPersistService(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Nullable @Named("hibernate") Properties properties, Interceptor interceptor) {
		this.modelProviders = modelProviders;
		this.physicalNamingStrategy = physicalNamingStrategy;
		this.properties = properties;
		this.interceptor = interceptor;
	}

	@Override
	public void start() {
		Preconditions.checkState(sessionFactory == null);

		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
				.applySettings(properties).build();

		MetadataSources metadataSources = new MetadataSources(standardRegistry);
		for (Class<? extends AbstractEntity> each: ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
			metadataSources.addAnnotatedClass(each);
		}
		
		for (ModelProvider provider: modelProviders) {
			for (Class<? extends AbstractEntity> modelClass: provider.getModelClasses())
				metadataSources.addAnnotatedClass(modelClass);
		}
		
		Metadata metadata = metadataSources.getMetadataBuilder()
				.applyPhysicalNamingStrategy(physicalNamingStrategy)
				.build();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
	}

	@Override
	public void stop() {
		if (sessionFactory != null) {
			Preconditions.checkState(!sessionFactory.isClosed());
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	@Override
	public SessionFactory get() {
		Preconditions.checkNotNull(sessionFactory, "Persist service is either not started or is not configured.");
		return sessionFactory;
	}

}
