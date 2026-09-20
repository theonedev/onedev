package io.onedev.server.persistence;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.STATIC_METAMODEL_POPULATION;

import java.util.Properties;
import java.util.Set;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;

import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.AbstractEntity;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
		// We use the runtime JPA metamodel, but do not generate static metamodel classes.
		hibernateSettings.putIfAbsent(STATIC_METAMODEL_POPULATION, "disabled");
		// Keep the configured dialect for OneDev's upgrade/maintenance decisions,
		// but let Hibernate detect standard dialects from JDBC metadata.
		if (!"false".equalsIgnoreCase(hibernateSettings.getProperty("hibernate.boot.allow_jdbc_metadata_access"))
				&& Set.of("org.hibernate.dialect.HSQLDialect", "org.hibernate.dialect.MySQLDialect",
						"org.hibernate.dialect.MariaDBDialect")
						.contains(hibernateSettings.getProperty(DIALECT, ""))) {
			hibernateSettings.remove(DIALECT);
		}
		StandardServiceRegistry serviceRegistry = null;
		try {
			if (hazelcastInstance != null) {
				hibernateSettings.put("hibernate.cache.hazelcast.instance_name", hazelcastInstance.getName());
				// The cluster service owns the shared member, including its shutdown.
				hibernateSettings.put("hibernate.cache.hazelcast.shutdown_on_session_factory_close", "false");
			} else {
				hibernateSettings.put("hibernate.cache.use_second_level_cache", "false");
				hibernateSettings.put("hibernate.cache.use_query_cache", "false");
				hibernateSettings.put("hibernate.hikari.maximumPoolSize", "1");
			}
			serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(hibernateSettings).build();
			MetadataSources metadataSources = new MetadataSources(serviceRegistry);
			for (Class<? extends AbstractEntity> each:
					ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
				metadataSources.addAnnotatedClass(each);
			}

			MetadataBuilder builder = metadataSources.getMetadataBuilder();
			metadata = builder.applyPhysicalNamingStrategy(physicalNamingStrategy).build();
			sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
		} catch (RuntimeException | Error e) {
			try {
				if (serviceRegistry != null)
					StandardServiceRegistryBuilder.destroy(serviceRegistry);
			} finally {
				stop();
			}
			throw e;
		}
	}

	@Override
	public void stop() {
		try {
			if (sessionFactory != null)
				sessionFactory.close();
		} finally {
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
