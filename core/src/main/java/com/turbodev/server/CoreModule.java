package com.turbodev.server;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.type.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turbodev.launcher.bootstrap.Bootstrap;
import com.turbodev.launcher.loader.AbstractPlugin;
import com.turbodev.launcher.loader.AbstractPluginModule;
import com.turbodev.utils.ClassUtils;
import com.turbodev.utils.schedule.DefaultTaskScheduler;
import com.turbodev.utils.schedule.TaskScheduler;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601SqlTimestampConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.turbodev.server.command.ApplyDBConstraintsCommand;
import com.turbodev.server.command.BackupDBCommand;
import com.turbodev.server.command.CheckDataVersionCommand;
import com.turbodev.server.command.CleanDBCommand;
import com.turbodev.server.command.CommandNames;
import com.turbodev.server.command.DBDialectCommand;
import com.turbodev.server.command.ResetAdminPasswordCommand;
import com.turbodev.server.command.RestoreDBCommand;
import com.turbodev.server.command.UpgradeCommand;
import com.turbodev.server.git.config.GitConfig;
import com.turbodev.server.git.jackson.GitObjectMapperConfigurator;
import com.turbodev.server.manager.AttachmentManager;
import com.turbodev.server.manager.BatchWorkManager;
import com.turbodev.server.manager.BranchWatchManager;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.manager.CodeCommentManager;
import com.turbodev.server.manager.CodeCommentRelationInfoManager;
import com.turbodev.server.manager.CodeCommentRelationManager;
import com.turbodev.server.manager.CodeCommentReplyManager;
import com.turbodev.server.manager.CommitInfoManager;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.DataManager;
import com.turbodev.server.manager.GroupAuthorizationManager;
import com.turbodev.server.manager.GroupManager;
import com.turbodev.server.manager.MailManager;
import com.turbodev.server.manager.MarkdownManager;
import com.turbodev.server.manager.MembershipManager;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.manager.PullRequestCommentManager;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.manager.PullRequestReferenceManager;
import com.turbodev.server.manager.PullRequestStatusChangeManager;
import com.turbodev.server.manager.PullRequestTaskManager;
import com.turbodev.server.manager.PullRequestUpdateManager;
import com.turbodev.server.manager.PullRequestWatchManager;
import com.turbodev.server.manager.ReviewInvitationManager;
import com.turbodev.server.manager.ReviewManager;
import com.turbodev.server.manager.StorageManager;
import com.turbodev.server.manager.UserAuthorizationManager;
import com.turbodev.server.manager.UserInfoManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.manager.VerificationManager;
import com.turbodev.server.manager.VisitManager;
import com.turbodev.server.manager.WorkExecutor;
import com.turbodev.server.manager.impl.DefaultAttachmentManager;
import com.turbodev.server.manager.impl.DefaultBatchWorkManager;
import com.turbodev.server.manager.impl.DefaultBranchWatchManager;
import com.turbodev.server.manager.impl.DefaultCacheManager;
import com.turbodev.server.manager.impl.DefaultCodeCommentManager;
import com.turbodev.server.manager.impl.DefaultCodeCommentRelationInfoManager;
import com.turbodev.server.manager.impl.DefaultCodeCommentRelationManager;
import com.turbodev.server.manager.impl.DefaultCodeCommentReplyManager;
import com.turbodev.server.manager.impl.DefaultCommitInfoManager;
import com.turbodev.server.manager.impl.DefaultConfigManager;
import com.turbodev.server.manager.impl.DefaultDataManager;
import com.turbodev.server.manager.impl.DefaultGroupAuthorizationManager;
import com.turbodev.server.manager.impl.DefaultGroupManager;
import com.turbodev.server.manager.impl.DefaultMailManager;
import com.turbodev.server.manager.impl.DefaultMarkdownManager;
import com.turbodev.server.manager.impl.DefaultMembershipManager;
import com.turbodev.server.manager.impl.DefaultNotificationManager;
import com.turbodev.server.manager.impl.DefaultProjectManager;
import com.turbodev.server.manager.impl.DefaultPullRequestCommentManager;
import com.turbodev.server.manager.impl.DefaultPullRequestManager;
import com.turbodev.server.manager.impl.DefaultPullRequestReferenceManager;
import com.turbodev.server.manager.impl.DefaultPullRequestStatusChangeManager;
import com.turbodev.server.manager.impl.DefaultPullRequestTaskManager;
import com.turbodev.server.manager.impl.DefaultPullRequestUpdateManager;
import com.turbodev.server.manager.impl.DefaultPullRequestWatchManager;
import com.turbodev.server.manager.impl.DefaultReviewInvitationManager;
import com.turbodev.server.manager.impl.DefaultReviewManager;
import com.turbodev.server.manager.impl.DefaultStorageManager;
import com.turbodev.server.manager.impl.DefaultUserAuthorizationManager;
import com.turbodev.server.manager.impl.DefaultUserInfoManager;
import com.turbodev.server.manager.impl.DefaultUserManager;
import com.turbodev.server.manager.impl.DefaultVerificationManager;
import com.turbodev.server.manager.impl.DefaultVisitManager;
import com.turbodev.server.manager.impl.DefaultWorkExecutor;
import com.turbodev.server.manager.impl.NotificationManager;
import com.turbodev.server.migration.JpaConverter;
import com.turbodev.server.migration.PersistentBagConverter;
import com.turbodev.server.persistence.DefaultIdManager;
import com.turbodev.server.persistence.DefaultPersistManager;
import com.turbodev.server.persistence.DefaultUnitOfWork;
import com.turbodev.server.persistence.HibernateInterceptor;
import com.turbodev.server.persistence.IdManager;
import com.turbodev.server.persistence.PersistListener;
import com.turbodev.server.persistence.PersistManager;
import com.turbodev.server.persistence.PrefixedNamingStrategy;
import com.turbodev.server.persistence.SessionFactoryProvider;
import com.turbodev.server.persistence.SessionInterceptor;
import com.turbodev.server.persistence.SessionProvider;
import com.turbodev.server.persistence.TransactionInterceptor;
import com.turbodev.server.persistence.UnitOfWork;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.DefaultDao;
import com.turbodev.server.security.BasicAuthenticationFilter;
import com.turbodev.server.security.FilterChainConfigurator;
import com.turbodev.server.security.TurboDevAuthorizingRealm;
import com.turbodev.server.security.TurboDevFilterChainResolver;
import com.turbodev.server.security.TurboDevPasswordService;
import com.turbodev.server.security.TurboDevRememberMeManager;
import com.turbodev.server.security.TurboDevWebSecurityManager;
import com.turbodev.server.security.authenticator.Authenticator;
import com.turbodev.server.util.jackson.ObjectMapperConfigurator;
import com.turbodev.server.util.jackson.ObjectMapperProvider;
import com.turbodev.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import com.turbodev.server.util.jetty.DefaultJettyRunner;
import com.turbodev.server.util.jetty.JettyRunner;
import com.turbodev.server.util.validation.DefaultEntityValidator;
import com.turbodev.server.util.validation.EntityValidator;
import com.turbodev.server.util.validation.ValidatorProvider;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(JettyRunner.class).to(DefaultJettyRunner.class);
		bind(ServletContextHandler.class).toProvider(DefaultJettyRunner.class);
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
		
		bind(ValidatorFactory.class).toProvider(new com.google.inject.Provider<ValidatorFactory>() {

			@Override
			public ValidatorFactory get() {
				Configuration<?> configuration = Validation.byDefaultProvider().configure();
				return configuration.buildValidatorFactory();
			}
			
		}).in(Singleton.class);
		
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);

		// configure markdown
		bind(MarkdownManager.class).to(DefaultMarkdownManager.class);		
		
		configurePersistence();
		
		bind(GitConfig.class).toProvider(GitConfigProvider.class);

		/*
		 * Declare bindings explicitly instead of using ImplementedBy annotation as
		 * HK2 to guice bridge can only search in explicit bindings in Guice   
		 */
		bind(StorageManager.class).to(DefaultStorageManager.class);
		bind(ConfigManager.class).to(DefaultConfigManager.class);
		bind(DataManager.class).to(DefaultDataManager.class);
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class).in(Singleton.class);
		bind(PullRequestCommentManager.class).to(DefaultPullRequestCommentManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(PullRequestManager.class).to(DefaultPullRequestManager.class);
		bind(PullRequestUpdateManager.class).to(DefaultPullRequestUpdateManager.class);
		bind(ProjectManager.class).to(DefaultProjectManager.class);
		bind(UserManager.class).to(DefaultUserManager.class);
		bind(ReviewInvitationManager.class).to(DefaultReviewInvitationManager.class);
		bind(ReviewManager.class).to(DefaultReviewManager.class);
		bind(MailManager.class).to(DefaultMailManager.class);
		bind(BranchWatchManager.class).to(DefaultBranchWatchManager.class);
		bind(PullRequestTaskManager.class).to(DefaultPullRequestTaskManager.class);
		bind(PullRequestWatchManager.class).to(DefaultPullRequestWatchManager.class);
		bind(CommitInfoManager.class).to(DefaultCommitInfoManager.class);
		bind(VisitManager.class).to(DefaultVisitManager.class);
		bind(UserInfoManager.class).to(DefaultUserInfoManager.class);
		bind(BatchWorkManager.class).to(DefaultBatchWorkManager.class);
		bind(GroupManager.class).to(DefaultGroupManager.class);
		bind(MembershipManager.class).to(DefaultMembershipManager.class);
		bind(GroupAuthorizationManager.class).to(DefaultGroupAuthorizationManager.class);
		bind(UserAuthorizationManager.class).to(DefaultUserAuthorizationManager.class);
		bind(PullRequestStatusChangeManager.class).to(DefaultPullRequestStatusChangeManager.class);
		bind(CodeCommentReplyManager.class).to(DefaultCodeCommentReplyManager.class);
		bind(AttachmentManager.class).to(DefaultAttachmentManager.class);
		bind(CodeCommentRelationInfoManager.class).to(DefaultCodeCommentRelationInfoManager.class);
		bind(CodeCommentRelationManager.class).to(DefaultCodeCommentRelationManager.class);
		bind(PullRequestReferenceManager.class).to(DefaultPullRequestReferenceManager.class);
		bind(WorkExecutor.class).to(DefaultWorkExecutor.class);
		bind(NotificationManager.class).to(DefaultNotificationManager.class);
		bind(CacheManager.class).to(DefaultCacheManager.class);
		bind(VerificationManager.class).to(DefaultVerificationManager.class);
		bind(Session.class).toProvider(SessionProvider.class);
		bind(EntityManager.class).toProvider(SessionProvider.class);
		bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
		bind(EntityManagerFactory.class).toProvider(SessionFactoryProvider.class);

		contribute(ObjectMapperConfigurator.class, GitObjectMapperConfigurator.class);
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    
		bind(Realm.class).to(TurboDevAuthorizingRealm.class);
		bind(RememberMeManager.class).to(TurboDevRememberMeManager.class);
		bind(WebSecurityManager.class).to(TurboDevWebSecurityManager.class);
		bind(FilterChainResolver.class).to(TurboDevFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(PasswordService.class).to(TurboDevPasswordService.class);
		bind(ShiroFilter.class);
		install(new ShiroAopModule());
        contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

            @Override
            public void configure(FilterChainManager filterChainManager) {
                filterChainManager.createChain("/**/info/refs", "noSessionCreation, authcBasic");
                filterChainManager.createChain("/**/git-upload-pack", "noSessionCreation, authcBasic");
                filterChainManager.createChain("/**/git-receive-pack", "noSessionCreation, authcBasic");
            }
            
        });
        contributeFromPackage(Authenticator.class, Authenticator.class);
        
		bind(EntityValidator.class).to(DefaultEntityValidator.class);
	}

	private void configurePersistence() {
		// Use an optional binding here in case our client does not like to 
		// start persist service provided by this plugin
		bind(Interceptor.class).to(HibernateInterceptor.class);
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("g_"));
		
		bind(UnitOfWork.class).to(DefaultUnitOfWork.class);
		bind(IdManager.class).to(DefaultIdManager.class);
		bind(Dao.class).to(DefaultDao.class);
		
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
	    
		bind(XStream.class).toProvider(new com.google.inject.Provider<XStream>() {

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
			if (CommandNames.RESTORE.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(RestoreDBCommand.class);
			else if (CommandNames.APPLY_DB_CONSTRAINTS.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(ApplyDBConstraintsCommand.class);
			else if (CommandNames.BACKUP.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(BackupDBCommand.class);
			else if (CommandNames.CHECK_DATA_VERSION.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(CheckDataVersionCommand.class);
			else if (CommandNames.UPGRADE.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(UpgradeCommand.class);
			else if (CommandNames.CLEAN.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(CleanDBCommand.class);
			else if (CommandNames.DB_DIALECT.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(DBDialectCommand.class);
			else if (CommandNames.RESET_ADMIN_PASSWORD.equals(Bootstrap.command.getName()))
				bind(PersistManager.class).to(ResetAdminPasswordCommand.class);
			else
				throw new RuntimeException("Unrecognized command: " + Bootstrap.command.getName());
		} else {
			bind(PersistManager.class).to(DefaultPersistManager.class);
		}		
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return TurboDev.class;
	}

}
