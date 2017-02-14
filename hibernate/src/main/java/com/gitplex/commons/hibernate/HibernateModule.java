package com.gitplex.commons.hibernate;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.type.Type;

import com.gitplex.commons.hibernate.command.DefaultApplyDBConstraintsCommand;
import com.gitplex.commons.hibernate.command.DefaultBackupCommand;
import com.gitplex.commons.hibernate.command.DefaultCheckDataVersionCommand;
import com.gitplex.commons.hibernate.command.DefaultCleanCommand;
import com.gitplex.commons.hibernate.command.DefaultDBDialectCommand;
import com.gitplex.commons.hibernate.command.DefaultRestoreCommand;
import com.gitplex.commons.hibernate.command.DefaultUpgradeCommand;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.DefaultDao;
import com.gitplex.commons.hibernate.jackson.HibernateObjectMapperConfigurator;
import com.gitplex.commons.hibernate.migration.JpaConverter;
import com.gitplex.commons.hibernate.migration.PersistentBagConverter;
import com.gitplex.commons.hibernate.migration.VersionTable;
import com.google.common.collect.Lists;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.gitplex.calla.bootstrap.Bootstrap;
import com.gitplex.calla.bootstrap.Command;
import com.gitplex.commons.jackson.ObjectMapperConfigurator;
import com.gitplex.commons.jetty.ServletConfigurator;
import com.gitplex.calla.loader.AbstractPlugin;
import com.gitplex.calla.loader.AbstractPluginModule;
import com.gitplex.commons.util.ClassUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601SqlTimestampConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class HibernateModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		// Use an optional binding here in case our client does not like to 
		// start persist service provided by this plugin
		bind(Interceptor.class).to(HibernateInterceptor.class);
		bind(PhysicalNamingStrategy.class).to(PhysicalNamingStrategyStandardImpl.class);
		
		bind(UnitOfWork.class).to(DefaultUnitOfWork.class);
		bind(IdManager.class).to(DefaultIdManager.class);
		bind(Dao.class).to(DefaultDao.class);
		bind(EntityValidator.class).to(DefaultEntityValidator.class);
		
	    TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
	    requestInjection(transactionInterceptor);
	    
	    bindInterceptor(Matchers.any(), new AbstractMatcher<AnnotatedElement>() {

			@Override
			public boolean matches(AnnotatedElement element) {
				return element.isAnnotationPresent(Transactional.class) && !((Method) element).isSynthetic();
			}
	    	
	    }, transactionInterceptor);
	    
	    SessionInterceptor sessionInterceptor = new SessionInterceptor();
	    requestInjection(sessionInterceptor);
	    
	    bindInterceptor(Matchers.any(), new AbstractMatcher<AnnotatedElement>() {

			@Override
			public boolean matches(AnnotatedElement element) {
				return element.isAnnotationPresent(Sessional.class) && !((Method) element).isSynthetic();
			}
	    	
	    }, sessionInterceptor);
	    
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    contribute(ServletConfigurator.class, HibernateServletConfigurator.class);
	    
	    contribute(PersistListener.class, new PersistListener() {
			
			@Override
			public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
				return false;
			}
			
			@Override
			public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
				return false;
			}
			
			@Override
			public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
					String[] propertyNames, Type[] types) throws CallbackException {
				return false;
			}
			
			@Override
			public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
			}
		});
	    
		contribute(ModelProvider.class, new ModelProvider() {

			@SuppressWarnings("unchecked")
			@Override
			public Collection<Class<? extends AbstractEntity>> getModelClasses() {
				return Lists.newArrayList(VersionTable.class);
			}
			
		});
		
		// put your guice bindings here
		bind(XStream.class).toProvider(new Provider<XStream>() {

			@SuppressWarnings("rawtypes")
			@Override
			public XStream get() {
				ReflectionProvider reflectionProvider = JVM.newReflectionProvider();
				XStream xstream = new XStream(reflectionProvider) {

					@Override
					protected MapperWrapper wrapMapper(MapperWrapper next) {
						return new MapperWrapper(next) {
							
							@Override
							public boolean shouldSerializeMember(Class definedIn, String fieldName) {
								Field field = reflectionProvider.getField(definedIn, fieldName);
								
								return field.getAnnotation(XStreamOmitField.class) == null && 
										field.getAnnotation(Transient.class) == null && 
										field.getAnnotation(OneToMany.class) == null &&
										field.getAnnotation(Version.class) == null;
							}
							
							@SuppressWarnings("unchecked")
							@Override
							public String serializedClass(Class type) {
								if (type == PersistentBag.class)
									return super.serializedClass(ArrayList.class);
								else if (type != null)
									return super.serializedClass(ClassUtils.unproxy(type));
								else
									return super.serializedClass(type);
							}
							
						};
					}
					
				};
				
				// register NullConverter as highest; otherwise NPE when unmarshal a map 
				// containing an entry with value set to null.
				xstream.registerConverter(new NullConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new PersistentBagConverter(xstream.getMapper()), 200);
				xstream.registerConverter(new JpaConverter(xstream.getMapper(), xstream.getReflectionProvider()));
				xstream.registerConverter(new ISO8601DateConverter(), 100);
				xstream.registerConverter(new ISO8601SqlTimestampConverter(), 100); 
				xstream.autodetectAnnotations(true);
				
				return xstream;
			}
			
		}).in(Singleton.class);
		
		if (Bootstrap.command != null) {
			if (Command.RESTORE.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultRestoreCommand.class);
			else if (Command.APPLY_DB_CONSTRAINTS.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultApplyDBConstraintsCommand.class);
			else if (Command.BACKUP.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultBackupCommand.class);
			else if (Command.CHECK_DATA_VERSION.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultCheckDataVersionCommand.class);
			else if (Command.UPGRADE.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultUpgradeCommand.class);
			else if (Command.CLEAN.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultCleanCommand.class);
			else if (Command.DB_DIALECT.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DefaultDBDialectCommand.class);
			else
				bind(PersistManager.class).to(DefaultPersistManager.class);
		} else {
			bind(PersistManager.class).to(DefaultPersistManager.class);
		}
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return HibernatePlugin.class;
	}
	
}
