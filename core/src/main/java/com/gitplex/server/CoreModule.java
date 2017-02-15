package com.gitplex.server;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.type.Type;
import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.launcher.loader.AbstractPlugin;
import com.gitplex.launcher.loader.AbstractPluginModule;
import com.gitplex.launcher.loader.ImplementationProvider;
import com.gitplex.launcher.loader.LoaderUtils;
import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.launcher.bootstrap.Command;
import com.gitplex.server.command.DefaultApplyDBConstraintsCommand;
import com.gitplex.server.command.DefaultBackupCommand;
import com.gitplex.server.command.DefaultCheckDataVersionCommand;
import com.gitplex.server.command.DefaultCleanCommand;
import com.gitplex.server.command.DefaultDBDialectCommand;
import com.gitplex.server.command.DefaultRestoreCommand;
import com.gitplex.server.command.DefaultUpgradeCommand;
import com.gitplex.server.command.ResetAdminPasswordCommand;
import com.gitplex.server.entity.EntityLocator;
import com.gitplex.server.git.config.GitConfig;
import com.gitplex.server.git.config.SpecifiedGit;
import com.gitplex.server.git.config.SystemGit;
import com.gitplex.server.git.jackson.GitObjectMapperConfigurator;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.BranchWatchManager;
import com.gitplex.server.manager.CodeCommentInfoManager;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentRelationManager;
import com.gitplex.server.manager.CodeCommentReplyManager;
import com.gitplex.server.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.DataManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.manager.OrganizationMembershipManager;
import com.gitplex.server.manager.PullRequestCommentManager;
import com.gitplex.server.manager.PullRequestInfoManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestReferenceManager;
import com.gitplex.server.manager.PullRequestReviewInvitationManager;
import com.gitplex.server.manager.PullRequestReviewManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.PullRequestTaskManager;
import com.gitplex.server.manager.PullRequestUpdateManager;
import com.gitplex.server.manager.PullRequestVerificationManager;
import com.gitplex.server.manager.PullRequestWatchManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.TeamAuthorizationManager;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.manager.TeamMembershipManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.manager.VisitInfoManager;
import com.gitplex.server.manager.WorkExecutor;
import com.gitplex.server.manager.impl.DefaultAccountManager;
import com.gitplex.server.manager.impl.DefaultAttachmentManager;
import com.gitplex.server.manager.impl.DefaultBatchWorkManager;
import com.gitplex.server.manager.impl.DefaultBranchWatchManager;
import com.gitplex.server.manager.impl.DefaultCodeCommentInfoManager;
import com.gitplex.server.manager.impl.DefaultCodeCommentManager;
import com.gitplex.server.manager.impl.DefaultCodeCommentRelationManager;
import com.gitplex.server.manager.impl.DefaultCodeCommentReplyManager;
import com.gitplex.server.manager.impl.DefaultCodeCommentStatusChangeManager;
import com.gitplex.server.manager.impl.DefaultCommitInfoManager;
import com.gitplex.server.manager.impl.DefaultConfigManager;
import com.gitplex.server.manager.impl.DefaultDataManager;
import com.gitplex.server.manager.impl.DefaultDepotManager;
import com.gitplex.server.manager.impl.DefaultMailManager;
import com.gitplex.server.manager.impl.DefaultOrganizationMembershipManager;
import com.gitplex.server.manager.impl.DefaultPullRequestCommentManager;
import com.gitplex.server.manager.impl.DefaultPullRequestInfoManager;
import com.gitplex.server.manager.impl.DefaultPullRequestManager;
import com.gitplex.server.manager.impl.DefaultPullRequestReferenceManager;
import com.gitplex.server.manager.impl.DefaultPullRequestReviewInvitationManager;
import com.gitplex.server.manager.impl.DefaultPullRequestReviewManager;
import com.gitplex.server.manager.impl.DefaultPullRequestStatusChangeManager;
import com.gitplex.server.manager.impl.DefaultPullRequestTaskManager;
import com.gitplex.server.manager.impl.DefaultPullRequestUpdateManager;
import com.gitplex.server.manager.impl.DefaultPullRequestVerificationManager;
import com.gitplex.server.manager.impl.DefaultPullRequestWatchManager;
import com.gitplex.server.manager.impl.DefaultStorageManager;
import com.gitplex.server.manager.impl.DefaultTeamAuthorizationManager;
import com.gitplex.server.manager.impl.DefaultTeamManager;
import com.gitplex.server.manager.impl.DefaultTeamMembershipManager;
import com.gitplex.server.manager.impl.DefaultUserAuthorizationManager;
import com.gitplex.server.manager.impl.DefaultVisitInfoManager;
import com.gitplex.server.manager.impl.DefaultWorkExecutor;
import com.gitplex.server.migration.DatabaseMigrator;
import com.gitplex.server.migration.JpaConverter;
import com.gitplex.server.migration.Migrator;
import com.gitplex.server.migration.PersistentBagConverter;
import com.gitplex.server.migration.VersionTable;
import com.gitplex.server.persistence.AbstractEntity;
import com.gitplex.server.persistence.DefaultIdManager;
import com.gitplex.server.persistence.DefaultPersistManager;
import com.gitplex.server.persistence.DefaultUnitOfWork;
import com.gitplex.server.persistence.HibernateInterceptor;
import com.gitplex.server.persistence.IdManager;
import com.gitplex.server.persistence.ModelProvider;
import com.gitplex.server.persistence.PersistListener;
import com.gitplex.server.persistence.PersistManager;
import com.gitplex.server.persistence.PrefixedNamingStrategy;
import com.gitplex.server.persistence.SessionInterceptor;
import com.gitplex.server.persistence.TransactionInterceptor;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.DefaultDao;
import com.gitplex.server.security.BasicAuthenticationFilter;
import com.gitplex.server.security.DefaultFilterChainResolver;
import com.gitplex.server.security.DefaultWebSecurityManager;
import com.gitplex.server.security.FilterChainConfigurator;
import com.gitplex.server.security.SecurityRealm;
import com.gitplex.server.util.ClassUtils;
import com.gitplex.server.util.jackson.ObjectMapperConfigurator;
import com.gitplex.server.util.jackson.ObjectMapperProvider;
import com.gitplex.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import com.gitplex.server.util.jetty.DefaultJettyRunner;
import com.gitplex.server.util.jetty.JettyRunner;
import com.gitplex.server.util.markdown.DefaultMarkdownManager;
import com.gitplex.server.util.markdown.HtmlTransformer;
import com.gitplex.server.util.markdown.MarkdownExtension;
import com.gitplex.server.util.markdown.MarkdownManager;
import com.gitplex.server.util.markdown.NormalizeTransformer;
import com.gitplex.server.util.schedule.DefaultTaskScheduler;
import com.gitplex.server.util.schedule.TaskScheduler;
import com.gitplex.server.util.validation.AccountNameReservation;
import com.gitplex.server.util.validation.DefaultEntityValidator;
import com.gitplex.server.util.validation.DepotNameReservation;
import com.gitplex.server.util.validation.EntityValidator;
import com.gitplex.server.util.validation.ValidatorProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.pmease.security.shiro.bcrypt.BCryptPasswordService;
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
		
		// put your guice bindings here
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
		
		contribute(MarkdownExtension.class, new MarkdownExtension() {
			
			@Override
			public Collection<Class<? extends Parser>> getInlineParsers() {
				return null;
			}
			
			@Override
			public Collection<ToHtmlSerializerPlugin> getHtmlSerializers() {
				return null;
			}
			
			@Override
			public Collection<Class<? extends Parser>> getBlockParsers() {
				return null;
			}

			@Override
			public Collection<HtmlTransformer> getHtmlTransformers() {
				return Lists.newArrayList((HtmlTransformer)new NormalizeTransformer());
			}
			
		});
		bind(MarkdownManager.class).to(DefaultMarkdownManager.class);		
		
		configurePersistence();
		
		bind(Migrator.class).to(DatabaseMigrator.class);
		
		contribute(ModelProvider.class, new ModelProvider() {

			@Override
			public Collection<Class<? extends AbstractEntity>> getModelClasses() {
				Collection<Class<? extends AbstractEntity>> modelClasses = 
						new HashSet<Class<? extends AbstractEntity>>();
				modelClasses.addAll(LoaderUtils.findImplementations(AbstractEntity.class, EntityLocator.class));
				return modelClasses;
			}
			
		});
		
		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(AccountNameReservation.class, new AccountNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});

		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(DepotNameReservation.class, new DepotNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {
			
			@Override
			public Collection<Class<?>> getImplementations() {
				Collection<Class<?>> implementations = new HashSet<Class<?>>();
				implementations.add(SystemGit.class);
				implementations.add(SpecifiedGit.class);
				return implementations;
			}
			
			@Override
			public Class<?> getAbstractClass() {
				return GitConfig.class;
			}
		});
		
		bind(GitConfig.class).toProvider(GitConfigProvider.class);

		/*
		 * Declare bindings explicitly instead of using ImplementedBy annotation as
		 * HK2 to guice bridge can only search in explicit bindings in Guice   
		 */
		bind(StorageManager.class).to(DefaultStorageManager.class);
		bind(ConfigManager.class).to(DefaultConfigManager.class);
		bind(DataManager.class).to(DefaultDataManager.class);
		bind(PullRequestCommentManager.class).to(DefaultPullRequestCommentManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(PullRequestManager.class).to(DefaultPullRequestManager.class);
		bind(PullRequestUpdateManager.class).to(DefaultPullRequestUpdateManager.class);
		bind(DepotManager.class).to(DefaultDepotManager.class);
		bind(AccountManager.class).to(DefaultAccountManager.class);
		bind(PullRequestVerificationManager.class).to(DefaultPullRequestVerificationManager.class);
		bind(PullRequestReviewInvitationManager.class).to(DefaultPullRequestReviewInvitationManager.class);
		bind(PullRequestReviewManager.class).to(DefaultPullRequestReviewManager.class);
		bind(MailManager.class).to(DefaultMailManager.class);
		bind(BranchWatchManager.class).to(DefaultBranchWatchManager.class);
		bind(PullRequestTaskManager.class).to(DefaultPullRequestTaskManager.class);
		bind(PullRequestWatchManager.class).to(DefaultPullRequestWatchManager.class);
		bind(CommitInfoManager.class).to(DefaultCommitInfoManager.class);
		bind(PullRequestInfoManager.class).to(DefaultPullRequestInfoManager.class);
		bind(CodeCommentInfoManager.class).to(DefaultCodeCommentInfoManager.class);
		bind(VisitInfoManager.class).to(DefaultVisitInfoManager.class);
		bind(BatchWorkManager.class).to(DefaultBatchWorkManager.class);
		bind(TeamManager.class).to(DefaultTeamManager.class);
		bind(OrganizationMembershipManager.class).to(DefaultOrganizationMembershipManager.class);
		bind(TeamMembershipManager.class).to(DefaultTeamMembershipManager.class);
		bind(TeamAuthorizationManager.class).to(DefaultTeamAuthorizationManager.class);
		bind(UserAuthorizationManager.class).to(DefaultUserAuthorizationManager.class);
		bind(PullRequestStatusChangeManager.class).to(DefaultPullRequestStatusChangeManager.class);
		bind(CodeCommentReplyManager.class).to(DefaultCodeCommentReplyManager.class);
		bind(AttachmentManager.class).to(DefaultAttachmentManager.class);
		bind(CodeCommentRelationManager.class).to(DefaultCodeCommentRelationManager.class);
		bind(CodeCommentStatusChangeManager.class).to(DefaultCodeCommentStatusChangeManager.class);
		bind(PullRequestReferenceManager.class).to(DefaultPullRequestReferenceManager.class);
		bind(WorkExecutor.class).to(DefaultWorkExecutor.class);

		contribute(ObjectMapperConfigurator.class, GitObjectMapperConfigurator.class);
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    
		bind(AuthorizingRealm.class).to(SecurityRealm.class);
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(PasswordService.class).to(BCryptPasswordService.class).in(Singleton.class);
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
        
		bind(EntityValidator.class).to(DefaultEntityValidator.class);
		
		if (Bootstrap.command != null 
				&& Bootstrap.command.getName().equals("reset_admin_password")) {
			bind(PersistManager.class).to(ResetAdminPasswordCommand.class);
		}
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
	    
		contribute(ModelProvider.class, new ModelProvider() {

			@SuppressWarnings("unchecked")
			@Override
			public Collection<Class<? extends AbstractEntity>> getModelClasses() {
				return Lists.newArrayList(VersionTable.class);
			}
			
		});
		
		// put your guice bindings here
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
		return GitPlex.class;
	}

}
