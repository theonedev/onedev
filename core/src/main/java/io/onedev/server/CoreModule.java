package io.onedev.server;

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

import io.onedev.launcher.bootstrap.Bootstrap;
import io.onedev.launcher.loader.AbstractPlugin;
import io.onedev.launcher.loader.AbstractPluginModule;
import io.onedev.server.command.ApplyDBConstraintsCommand;
import io.onedev.server.command.BackupDBCommand;
import io.onedev.server.command.CheckDataVersionCommand;
import io.onedev.server.command.CleanDBCommand;
import io.onedev.server.command.CommandNames;
import io.onedev.server.command.DBDialectCommand;
import io.onedev.server.command.ResetAdminPasswordCommand;
import io.onedev.server.command.RestoreDBCommand;
import io.onedev.server.command.UpgradeCommand;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.git.jackson.GitObjectMapperConfigurator;
import io.onedev.server.manager.AttachmentManager;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.BranchWatchManager;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CodeCommentRelationManager;
import io.onedev.server.manager.CodeCommentReplyManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.DataManager;
import io.onedev.server.manager.GroupAuthorizationManager;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.MembershipManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestReferenceManager;
import io.onedev.server.manager.PullRequestStatusChangeManager;
import io.onedev.server.manager.PullRequestTaskManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.manager.PullRequestWatchManager;
import io.onedev.server.manager.ReviewInvitationManager;
import io.onedev.server.manager.ReviewManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.manager.UserAuthorizationManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.manager.VerificationManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.manager.WorkExecutor;
import io.onedev.server.manager.impl.DefaultAttachmentManager;
import io.onedev.server.manager.impl.DefaultBatchWorkManager;
import io.onedev.server.manager.impl.DefaultBranchWatchManager;
import io.onedev.server.manager.impl.DefaultCacheManager;
import io.onedev.server.manager.impl.DefaultCodeCommentManager;
import io.onedev.server.manager.impl.DefaultCodeCommentRelationInfoManager;
import io.onedev.server.manager.impl.DefaultCodeCommentRelationManager;
import io.onedev.server.manager.impl.DefaultCodeCommentReplyManager;
import io.onedev.server.manager.impl.DefaultCommitInfoManager;
import io.onedev.server.manager.impl.DefaultConfigManager;
import io.onedev.server.manager.impl.DefaultDataManager;
import io.onedev.server.manager.impl.DefaultGroupAuthorizationManager;
import io.onedev.server.manager.impl.DefaultGroupManager;
import io.onedev.server.manager.impl.DefaultMailManager;
import io.onedev.server.manager.impl.DefaultMarkdownManager;
import io.onedev.server.manager.impl.DefaultMembershipManager;
import io.onedev.server.manager.impl.DefaultNotificationManager;
import io.onedev.server.manager.impl.DefaultProjectManager;
import io.onedev.server.manager.impl.DefaultPullRequestCommentManager;
import io.onedev.server.manager.impl.DefaultPullRequestManager;
import io.onedev.server.manager.impl.DefaultPullRequestReferenceManager;
import io.onedev.server.manager.impl.DefaultPullRequestStatusChangeManager;
import io.onedev.server.manager.impl.DefaultPullRequestTaskManager;
import io.onedev.server.manager.impl.DefaultPullRequestUpdateManager;
import io.onedev.server.manager.impl.DefaultPullRequestWatchManager;
import io.onedev.server.manager.impl.DefaultReviewInvitationManager;
import io.onedev.server.manager.impl.DefaultReviewManager;
import io.onedev.server.manager.impl.DefaultStorageManager;
import io.onedev.server.manager.impl.DefaultUserAuthorizationManager;
import io.onedev.server.manager.impl.DefaultUserInfoManager;
import io.onedev.server.manager.impl.DefaultUserManager;
import io.onedev.server.manager.impl.DefaultVerificationManager;
import io.onedev.server.manager.impl.DefaultVisitManager;
import io.onedev.server.manager.impl.DefaultWorkExecutor;
import io.onedev.server.manager.impl.NotificationManager;
import io.onedev.server.migration.JpaConverter;
import io.onedev.server.migration.PersistentBagConverter;
import io.onedev.server.persistence.DefaultIdManager;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.DefaultUnitOfWork;
import io.onedev.server.persistence.HibernateInterceptor;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.PersistListener;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.SessionFactoryProvider;
import io.onedev.server.persistence.SessionInterceptor;
import io.onedev.server.persistence.SessionProvider;
import io.onedev.server.persistence.TransactionInterceptor;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.DefaultDao;
import io.onedev.server.security.BasicAuthenticationFilter;
import io.onedev.server.security.FilterChainConfigurator;
import io.onedev.server.security.OneDevAuthorizingRealm;
import io.onedev.server.security.OneDevFilterChainResolver;
import io.onedev.server.security.OneDevPasswordService;
import io.onedev.server.security.OneDevRememberMeManager;
import io.onedev.server.security.OneDevWebSecurityManager;
import io.onedev.server.security.authenticator.Authenticator;
import io.onedev.server.util.jackson.ObjectMapperConfigurator;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import io.onedev.server.util.jetty.DefaultJettyRunner;
import io.onedev.server.util.jetty.JettyRunner;
import io.onedev.server.util.validation.DefaultEntityValidator;
import io.onedev.server.util.validation.EntityValidator;
import io.onedev.server.util.validation.ValidatorProvider;
import io.onedev.utils.ClassUtils;
import io.onedev.utils.schedule.DefaultTaskScheduler;
import io.onedev.utils.schedule.TaskScheduler;

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
	    
		bind(Realm.class).to(OneDevAuthorizingRealm.class);
		bind(RememberMeManager.class).to(OneDevRememberMeManager.class);
		bind(WebSecurityManager.class).to(OneDevWebSecurityManager.class);
		bind(FilterChainResolver.class).to(OneDevFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(PasswordService.class).to(OneDevPasswordService.class);
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
		return OneDev.class;
	}

}
