package io.onedev.server.persistence;

import java.util.Properties;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.AbstractEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultSessionFactoryService implements SessionFactoryService {

	@Inject
	private HibernateConfig hibernateConfig;

	@Inject
	private PhysicalNamingStrategy physicalNamingStrategy;

	@Inject
	private ClusterService clusterService;

	@Inject
	private Interceptor interceptor;
	
	private volatile Metadata metadata;
	
	private volatile SessionFactory sessionFactory;

	@Override
	public void start() {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
		Properties hibernateSettings = new Properties();
		hibernateSettings.putAll(hibernateConfig);
		if (hazelcastInstance != null) {
			hibernateSettings.put("hibernate.cache.hazelcast.instance_name", hazelcastInstance.getName());
		} else { 
			hibernateSettings.put("hibernate.cache.use_second_level_cache", "false");
			hibernateSettings.put("hibernate.cache.use_query_cache", "false");
			hibernateSettings.put("hibernate.hikari.maximumPoolSize", "1");
		}
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(hibernateSettings).build();
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
		}
		metadata = null;
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