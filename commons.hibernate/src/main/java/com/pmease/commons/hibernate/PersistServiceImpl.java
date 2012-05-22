package com.pmease.commons.hibernate;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.extensionpoints.ModelContribution;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.StringUtils;

@Singleton
public class PersistServiceImpl implements PersistService, Provider<SessionFactory> {

	private final PluginManager pluginManager;
	
	private final NamingStrategy namingStrategy;

	private final Properties hibernateProperties;
	
	private volatile SessionFactory sessionFactory;
	
	@Inject
	public PersistServiceImpl(PluginManager pluginManager, NamingStrategy namingStrategy, 
			@Hibernate Properties hibernateProperties) {
		this.pluginManager = pluginManager;
		this.namingStrategy = namingStrategy;
		this.hibernateProperties = hibernateProperties;
		
		String url = hibernateProperties.getProperty(Environment.URL);
		hibernateProperties.setProperty(Environment.URL, 
				StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
		String encryptedPassword = hibernateProperties.getProperty("hibernate.connection.encrypted_password");
		if (StringUtils.isNotBlank(encryptedPassword))
			hibernateProperties.setProperty(Environment.PASS, StringUtils.decrypt(encryptedPassword.trim()));
	}
	
	public void start() {
		Preconditions.checkState(sessionFactory == null);

		Configuration cfg = new Configuration();
		cfg.setNamingStrategy(namingStrategy);
		Collection<Class<AbstractEntity>> modelClasses = 
				ClassUtils.findSubClasses(AbstractEntity.class, AbstractEntity.class);
		for (Class<AbstractEntity> model: modelClasses) {
			if (!Modifier.isAbstract(model.getModifiers()))
				cfg.addAnnotatedClass(model);
		}
		
		Collection<ModelContribution> contributions = 
				pluginManager.getExtensions(ModelContribution.class);
		for (ModelContribution contribution: contributions) {
			for (Class<? extends AbstractEntity> modelClass: contribution.getModelClasses())
				cfg.addAnnotatedClass(modelClass);
		}
		
		cfg.setProperties(hibernateProperties);		
        sessionFactory = cfg.buildSessionFactory();
	}

	public void stop() {
		if (sessionFactory != null) {
			Preconditions.checkState(!sessionFactory.isClosed());
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	public SessionFactory get() {
		Preconditions.checkNotNull(sessionFactory);
		return sessionFactory;
	}

	public Properties getHibernateProperties() {
		return hibernateProperties;
	}
}
