package io.onedev.server.persistence;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.model.AbstractEntity;

@Singleton
public class DefaultSessionFactoryManager implements SessionFactoryManager {
	
	private final HibernateConfig hibernateConfig;
	
	private final PhysicalNamingStrategy physicalNamingStrategy;
	
	private final ClusterManager clusterManager;
	
	private final Interceptor interceptor;
	
	private volatile Metadata metadata;
	
	private volatile SessionFactory sessionFactory;
	
	@Inject
	public DefaultSessionFactoryManager(HibernateConfig hibernateConfig, ClusterManager clusterManager, 
			PhysicalNamingStrategy physicalNamingStrategy, Interceptor interceptor, 
			ExecutorService executorService, TransactionManager transactionManager) {
		this.hibernateConfig = hibernateConfig;
		this.physicalNamingStrategy = physicalNamingStrategy;
		this.clusterManager = clusterManager;
		this.interceptor = interceptor;
	}

	@Override
	public void start() {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		Properties effectiveConfig = new Properties();
		effectiveConfig.putAll(hibernateConfig);
		if (hazelcastInstance != null) {
			effectiveConfig.put("hibernate.cache.hazelcast.instance_name", hazelcastInstance.getName());
		} else { 
			effectiveConfig.put("hibernate.cache.use_second_level_cache", "false");
			effectiveConfig.put("hibernate.cache.use_query_cache", "false");
			effectiveConfig.put("hibernate.hikari.maximumPoolSize", "1");
		}
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(effectiveConfig).build();
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		for (Class<? extends AbstractEntity> each: 
				ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
			metadataSources.addAnnotatedClass(each);
		}

		MetadataBuilder builder = metadataSources.getMetadataBuilder();
		metadata = builder.applyPhysicalNamingStrategy(physicalNamingStrategy).build();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
	}

	@Override
	public void stop() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
			metadata = null;
		}
	}
	
	@Override
	public Metadata getMetadata() {
		return metadata;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}