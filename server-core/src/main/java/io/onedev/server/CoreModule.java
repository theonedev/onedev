package io.onedev.server;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.entitymanager.*;
import io.onedev.server.entitymanager.impl.*;
import nl.altindag.ssl.SSLFactory;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.type.Type;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
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
import com.vladsch.flexmark.util.misc.Extension;

import io.onedev.agent.ExecutorUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.OsInfo;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.attachment.DefaultAttachmentManager;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterResource;
import io.onedev.server.cluster.DefaultClusterManager;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.codequality.LineCoverageContribution;
import io.onedev.server.commandhandler.ApplyDatabaseConstraints;
import io.onedev.server.commandhandler.BackupDatabase;
import io.onedev.server.commandhandler.CheckDataVersion;
import io.onedev.server.commandhandler.CleanDatabase;
import io.onedev.server.commandhandler.ResetAdminPassword;
import io.onedev.server.commandhandler.RestoreDatabase;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.entityreference.DefaultEntityReferenceManager;
import io.onedev.server.entityreference.EntityReferenceManager;
import io.onedev.server.event.DefaultListenerRegistry;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.exception.ExceptionHandler;
import io.onedev.server.git.GitFilter;
import io.onedev.server.git.GitLfsFilter;
import io.onedev.server.git.GoGetFilter;
import io.onedev.server.git.SshCommandCreator;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.git.exception.GitException;
import io.onedev.server.git.hook.GitPostReceiveCallback;
import io.onedev.server.git.hook.GitPreReceiveCallback;
import io.onedev.server.git.service.DefaultGitService;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.signature.DefaultSignatureVerificationKeyLoader;
import io.onedev.server.git.signature.SignatureVerificationKeyLoader;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.infomanager.DefaultCommitInfoManager;
import io.onedev.server.infomanager.DefaultIssueInfoManager;
import io.onedev.server.infomanager.DefaultPullRequestInfoManager;
import io.onedev.server.infomanager.DefaultVisitInfoManager;
import io.onedev.server.infomanager.IssueInfoManager;
import io.onedev.server.infomanager.PullRequestInfoManager;
import io.onedev.server.infomanager.VisitInfoManager;
import io.onedev.server.jetty.DefaultJettyLauncher;
import io.onedev.server.jetty.JettyLauncher;
import io.onedev.server.job.DefaultJobManager;
import io.onedev.server.job.DefaultResourceAllocator;
import io.onedev.server.job.JobManager;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.log.DefaultLogManager;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.mail.DefaultMailManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.DefaultMarkdownManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MarkdownProcessor;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.notification.BuildNotificationManager;
import io.onedev.server.notification.CodeCommentNotificationManager;
import io.onedev.server.notification.CommitNotificationManager;
import io.onedev.server.notification.IssueNotificationManager;
import io.onedev.server.notification.PullRequestNotificationManager;
import io.onedev.server.notification.WebHookManager;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.DefaultDataManager;
import io.onedev.server.persistence.DefaultIdManager;
import io.onedev.server.persistence.DefaultSessionFactoryManager;
import io.onedev.server.persistence.DefaultSessionManager;
import io.onedev.server.persistence.DefaultTransactionManager;
import io.onedev.server.persistence.HibernateInterceptor;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.PersistListener;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.persistence.SessionFactoryProvider;
import io.onedev.server.persistence.SessionInterceptor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.SessionProvider;
import io.onedev.server.persistence.TransactionInterceptor;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.DefaultDao;
import io.onedev.server.persistence.exception.ConstraintViolationExceptionHandler;
import io.onedev.server.rest.ProjectResource;
import io.onedev.server.rest.exception.UnauthenticatedExceptionHandler;
import io.onedev.server.rest.jersey.DefaultServletContainer;
import io.onedev.server.rest.jersey.JerseyConfigurator;
import io.onedev.server.rest.jersey.ResourceConfigProvider;
import io.onedev.server.search.code.CodeIndexManager;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.search.code.DefaultCodeIndexManager;
import io.onedev.server.search.code.DefaultCodeSearchManager;
import io.onedev.server.search.entitytext.CodeCommentTextManager;
import io.onedev.server.search.entitytext.DefaultCodeCommentTextManager;
import io.onedev.server.search.entitytext.DefaultIssueTextManager;
import io.onedev.server.search.entitytext.DefaultPullRequestTextManager;
import io.onedev.server.search.entitytext.IssueTextManager;
import io.onedev.server.search.entitytext.PullRequestTextManager;
import io.onedev.server.security.BasicAuthenticationFilter;
import io.onedev.server.security.BearerAuthenticationFilter;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.DefaultFilterChainResolver;
import io.onedev.server.security.DefaultPasswordService;
import io.onedev.server.security.DefaultRememberMeManager;
import io.onedev.server.security.DefaultShiroFilterConfiguration;
import io.onedev.server.security.DefaultWebSecurityManager;
import io.onedev.server.security.FilterChainConfigurator;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.realm.AbstractAuthorizingRealm;
import io.onedev.server.ssh.CommandCreator;
import io.onedev.server.ssh.DefaultSshAuthenticator;
import io.onedev.server.ssh.DefaultSshManager;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshManager;
import io.onedev.server.storage.DefaultStorageManager;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.terminal.DefaultTerminalManager;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.DefaultBatchWorkManager;
import io.onedev.server.util.concurrent.DefaultWorkExecutor;
import io.onedev.server.util.concurrent.WorkExecutor;
import io.onedev.server.util.jackson.ObjectMapperConfigurator;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.jackson.git.GitObjectMapperConfigurator;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import io.onedev.server.util.schedule.DefaultTaskScheduler;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.util.script.ScriptContribution;
import io.onedev.server.util.validation.ValidatorProvider;
import io.onedev.server.util.xstream.CollectionConverter;
import io.onedev.server.util.xstream.HibernateProxyConverter;
import io.onedev.server.util.xstream.MapConverter;
import io.onedev.server.util.xstream.ReflectionConverter;
import io.onedev.server.util.xstream.StringConverter;
import io.onedev.server.util.xstream.VersionedDocumentConverter;
import io.onedev.server.web.DefaultUploadItemManager;
import io.onedev.server.web.DefaultUrlManager;
import io.onedev.server.web.DefaultWicketFilter;
import io.onedev.server.web.DefaultWicketServlet;
import io.onedev.server.web.ResourcePackScopeContribution;
import io.onedev.server.web.UploadItemManager;
import io.onedev.server.web.WebApplication;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.avatar.DefaultAvatarManager;
import io.onedev.server.web.component.diff.DiffRenderer;
import io.onedev.server.web.component.markdown.SourcePositionTrackExtension;
import io.onedev.server.web.component.markdown.emoji.EmojiExtension;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.DefaultEditSupportRegistry;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EditSupportLocator;
import io.onedev.server.web.editable.EditSupportRegistry;
import io.onedev.server.web.exception.PageExpiredExceptionHandler;
import io.onedev.server.web.mapper.BasePageMapper;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;
import io.onedev.server.web.page.layout.DefaultMainMenuCustomization;
import io.onedev.server.web.page.layout.MainMenuCustomization;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import io.onedev.server.web.page.test.TestPage;
import io.onedev.server.web.websocket.BuildEventBroadcaster;
import io.onedev.server.web.websocket.CodeCommentEventBroadcaster;
import io.onedev.server.web.websocket.CommitIndexedBroadcaster;
import io.onedev.server.web.websocket.DefaultWebSocketManager;
import io.onedev.server.web.websocket.IssueEventBroadcaster;
import io.onedev.server.web.websocket.PullRequestEventBroadcaster;
import io.onedev.server.web.websocket.WebSocketManager;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ListenerRegistry.class).to(DefaultListenerRegistry.class);
		bind(JettyLauncher.class).to(DefaultJettyLauncher.class);
		bind(ServletContextHandler.class).toProvider(DefaultJettyLauncher.class);
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
		
		bind(ValidatorFactory.class).toProvider(new com.google.inject.Provider<ValidatorFactory>() {

			@Override
			public ValidatorFactory get() {
				Configuration<?> configuration = Validation
						.byDefaultProvider()
						.configure()
						.messageInterpolator(new ParameterMessageInterpolator());
				return configuration.buildValidatorFactory();
			}
			
		}).in(Singleton.class);
		
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);

		configurePersistence();
		configureSecurity();
		configureRestful();
		configureWeb();
		configureGit();
		configureBuild();
		configureCluster();

		/*
		 * Declare bindings explicitly instead of using ImplementedBy annotation as
		 * HK2 to guice bridge can only search in explicit bindings in Guice   
		 */
		bind(SshAuthenticator.class).to(DefaultSshAuthenticator.class);
		bind(SshManager.class).to(DefaultSshManager.class);
		bind(MarkdownManager.class).to(DefaultMarkdownManager.class);		
		bind(StorageManager.class).to(DefaultStorageManager.class);
		bind(SettingManager.class).to(DefaultSettingManager.class);
		bind(DataManager.class).to(DefaultDataManager.class);
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
		bind(PullRequestCommentManager.class).to(DefaultPullRequestCommentManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(PullRequestManager.class).to(DefaultPullRequestManager.class);
		bind(PullRequestUpdateManager.class).to(DefaultPullRequestUpdateManager.class);
		bind(ProjectManager.class).to(DefaultProjectManager.class);
		bind(ProjectDynamicsManager.class).to(DefaultProjectDynamicsManager.class);
		bind(UserManager.class).to(DefaultUserManager.class);
		bind(UserInvitationManager.class).to(DefaultUserInvitationManager.class);
		bind(PullRequestReviewManager.class).to(DefaultPullRequestReviewManager.class);
		bind(BuildManager.class).to(DefaultBuildManager.class);
		bind(BuildDependenceManager.class).to(DefaultBuildDependenceManager.class);
		bind(JobManager.class).to(DefaultJobManager.class);
		bind(LogManager.class).to(DefaultLogManager.class);
		bind(MailManager.class).to(DefaultMailManager.class);
		bind(IssueManager.class).to(DefaultIssueManager.class);
		bind(IssueFieldManager.class).to(DefaultIssueFieldManager.class);
		bind(BuildParamManager.class).to(DefaultBuildParamManager.class);
		bind(UserAuthorizationManager.class).to(DefaultUserAuthorizationManager.class);
		bind(GroupAuthorizationManager.class).to(DefaultGroupAuthorizationManager.class);
		bind(PullRequestWatchManager.class).to(DefaultPullRequestWatchManager.class);
		bind(RoleManager.class).to(DefaultRoleManager.class);
		bind(CommitInfoManager.class).to(DefaultCommitInfoManager.class);
		bind(IssueInfoManager.class).to(DefaultIssueInfoManager.class);
		bind(VisitInfoManager.class).to(DefaultVisitInfoManager.class);
		bind(BatchWorkManager.class).to(DefaultBatchWorkManager.class);
		bind(WorkExecutor.class).to(DefaultWorkExecutor.class);
		bind(GroupManager.class).to(DefaultGroupManager.class);
		bind(IssueMentionManager.class).to(DefaultIssueMentionManager.class);
		bind(PullRequestMentionManager.class).to(DefaultPullRequestMentionManager.class);
		bind(CodeCommentMentionManager.class).to(DefaultCodeCommentMentionManager.class);
		bind(MembershipManager.class).to(DefaultMembershipManager.class);
		bind(PullRequestChangeManager.class).to(DefaultPullRequestChangeManager.class);
		bind(CodeCommentReplyManager.class).to(DefaultCodeCommentReplyManager.class);
		bind(CodeCommentStatusChangeManager.class).to(DefaultCodeCommentStatusChangeManager.class);
		bind(AttachmentManager.class).to(DefaultAttachmentManager.class);
		bind(PullRequestInfoManager.class).to(DefaultPullRequestInfoManager.class);
		bind(PullRequestNotificationManager.class);
		bind(CommitNotificationManager.class);
		bind(BuildNotificationManager.class);
		bind(IssueNotificationManager.class);
		bind(CodeCommentNotificationManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(IssueWatchManager.class).to(DefaultIssueWatchManager.class);
		bind(IssueChangeManager.class).to(DefaultIssueChangeManager.class);
		bind(IssueVoteManager.class).to(DefaultIssueVoteManager.class);
		bind(MilestoneManager.class).to(DefaultMilestoneManager.class);
		bind(IssueCommentManager.class).to(DefaultIssueCommentManager.class);
		bind(IssueQueryPersonalizationManager.class).to(DefaultIssueQueryPersonalizationManager.class);
		bind(PullRequestQueryPersonalizationManager.class).to(DefaultPullRequestQueryPersonalizationManager.class);
		bind(CodeCommentQueryPersonalizationManager.class).to(DefaultCodeCommentQueryPersonalizationManager.class);
		bind(CommitQueryPersonalizationManager.class).to(DefaultCommitQueryPersonalizationManager.class);
		bind(BuildQueryPersonalizationManager.class).to(DefaultBuildQueryPersonalizationManager.class);
		bind(PullRequestAssignmentManager.class).to(DefaultPullRequestAssignmentManager.class);
		bind(SshKeyManager.class).to(DefaultSshKeyManager.class);
		bind(BuildMetricManager.class).to(DefaultBuildMetricManager.class);
		bind(EntityReferenceManager.class).to(DefaultEntityReferenceManager.class);
		bind(GitLfsLockManager.class).to(DefaultGitLfsLockManager.class);
		bind(IssueScheduleManager.class).to(DefaultIssueScheduleManager.class);
		bind(LinkSpecManager.class).to(DefaultLinkSpecManager.class);
		bind(IssueLinkManager.class).to(DefaultIssueLinkManager.class);
		bind(LinkAuthorizationManager.class).to(DefaultLinkAuthorizationManager.class);
		bind(EmailAddressManager.class).to(DefaultEmailAddressManager.class);
		bind(GpgKeyManager.class).to(DefaultGpgKeyManager.class);
		bind(IssueTextManager.class).to(DefaultIssueTextManager.class);
		bind(PullRequestTextManager.class).to(DefaultPullRequestTextManager.class);
		bind(CodeCommentTextManager.class).to(DefaultCodeCommentTextManager.class);
		bind(PendingSuggestionApplyManager.class).to(DefaultPendingSuggestionApplyManager.class);
		bind(IssueAuthorizationManager.class).to(DefaultIssueAuthorizationManager.class);
		bind(DashboardManager.class).to(DefaultDashboardManager.class);
		bind(DashboardUserShareManager.class).to(DefaultDashboardUserShareManager.class);
		bind(DashboardGroupShareManager.class).to(DefaultDashboardGroupShareManager.class);
		bind(DashboardVisitManager.class).to(DefaultDashboardVisitManager.class);
		bind(LabelManager.class).to(DefaultLabelManager.class);
		bind(ProjectLabelManager.class).to(DefaultProjectLabelManager.class);
		bind(BuildLabelManager.class).to(DefaultBuildLabelManager.class);
		bind(PullRequestLabelManager.class).to(DefaultPullRequestLabelManager.class);
		
		bind(WebHookManager.class);
		
		contribute(CodePullAuthorizationSource.class, DefaultJobManager.class);
        
		bind(CodeIndexManager.class).to(DefaultCodeIndexManager.class);
		bind(CodeSearchManager.class).to(DefaultCodeSearchManager.class);

		Bootstrap.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, 
        		new SynchronousQueue<Runnable>()) {

			@Override
			public void execute(Runnable command) {
				try {
					super.execute(SecurityUtils.inheritSubject(command));
				} catch (RejectedExecutionException e) {
					if (!isShutdown())
						throw ExceptionUtils.unchecked(e);
				}
			}

        };

	    bind(ExecutorService.class).toProvider(new Provider<ExecutorService>() {

			@Override
			public ExecutorService get() {
				return Bootstrap.executorService;
			}
	    	
	    }).in(Singleton.class);
	    
	    bind(OsInfo.class).toProvider(new Provider<OsInfo>() {

			@Override
			public OsInfo get() {
				return ExecutorUtils.getOsInfo();
			}
	    	
	    }).in(Singleton.class);
	    
	    contributeFromPackage(LogInstruction.class, LogInstruction.class);
	    
	    
		contribute(CodeProblemContribution.class, new CodeProblemContribution() {
			
			@Override
			public List<CodeProblem> getCodeProblems(Build build, String blobPath, String reportName) {
				return Lists.newArrayList();
			}
			
		});
	    
		contribute(LineCoverageContribution.class, new LineCoverageContribution() {
			
			@Override
			public Map<Integer, CoverageStatus> getLineCoverages(Build build, String blobPath, String reportName) {
				return new HashMap<>();
			}
			
		});
		contribute(AdministrationSettingContribution.class, new AdministrationSettingContribution() {
			
			@Override
			public List<Class<? extends ContributedAdministrationSetting>> getSettingClasses() {
				return new ArrayList<>();
			}
			
		});
		contribute(ProjectSettingContribution.class, new ProjectSettingContribution() {
			
			@Override
			public List<Class<? extends ContributedProjectSetting>> getSettingClasses() {
				return new ArrayList<>();
			}
			
		});
		
	}
	
	private void configureCluster() {
		bind(ClusterCredentialManager.class).to(DefaultClusterCredentialManager.class);
		bind(ClusterServerManager.class).to(DefaultClusterServerManager.class);
		bind(ClusterManager.class).to(DefaultClusterManager.class);
	}
	
	private void configureSecurity() {
		contributeFromPackage(Realm.class, AbstractAuthorizingRealm.class);

		bind(ShiroFilterConfiguration.class).to(DefaultShiroFilterConfiguration.class);
		bind(RememberMeManager.class).to(DefaultRememberMeManager.class);
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(BearerAuthenticationFilter.class);
		bind(PasswordService.class).to(DefaultPasswordService.class);
		bind(ShiroFilter.class);
		install(new ShiroAopModule());
        contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

            @Override
            public void configure(FilterChainManager filterChainManager) {
                filterChainManager.createChain("/**/info/refs", "noSessionCreation, authcBasic, authcBearer");
                filterChainManager.createChain("/**/git-upload-pack", "noSessionCreation, authcBasic, authcBearer");
                filterChainManager.createChain("/**/git-receive-pack", "noSessionCreation, authcBasic, authcBearer");
            }
            
        });
        contributeFromPackage(Authenticator.class, Authenticator.class);
		
		bind(SSLFactory.class).toProvider(() -> {
			return KubernetesHelper.buildSSLFactory(Bootstrap.getTrustCertsDir());
		}).in(Singleton.class);
	}
	
	private void configureGit() {
		contribute(ObjectMapperConfigurator.class, GitObjectMapperConfigurator.class);
		bind(GitService.class).to(DefaultGitService.class);
		bind(GitLocation.class).toProvider(GitLocationProvider.class);
		bind(GitFilter.class);
		bind(GoGetFilter.class);
		bind(GitLfsFilter.class);
		bind(GitPreReceiveCallback.class);
		bind(GitPostReceiveCallback.class);
		bind(SignatureVerificationKeyLoader.class).to(DefaultSignatureVerificationKeyLoader.class);
		contribute(CommandCreator.class, SshCommandCreator.class);
	}
	
	private void configureRestful() {
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
		bind(ServletContainer.class).to(DefaultServletContainer.class);
		
		contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

			@Override
			public void configure(FilterChainManager filterChainManager) {
				filterChainManager.createChain("/~api/**", "noSessionCreation, authcBasic, authcBearer");
			}
			
		});
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.packages(ProjectResource.class.getPackage().getName());
			}
			
		});
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.register(ClusterResource.class);
			}
			
		});
	}

	private void configureWeb() {
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);

		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		bind(org.apache.wicket.protocol.http.WebApplication.class).to(WebApplication.class);
		bind(Application.class).to(WebApplication.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(org.apache.wicket.protocol.http.WebApplication application) {
				application.mount(new BasePageMapper("/~test", TestPage.class));
			}
			
		});
		
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRenderer.class, BlobRenderer.class);

		contribute(Extension.class, new EmojiExtension());
		contribute(Extension.class, new SourcePositionTrackExtension());
		
		contributeFromPackage(MarkdownProcessor.class, MarkdownProcessor.class);

		contribute(ResourcePackScopeContribution.class, new ResourcePackScopeContribution() {
			
			@Override
			public Collection<Class<?>> getResourcePackScopes() {
				return Lists.newArrayList(WebApplication.class);
			}
			
		});
		
		contributeFromPackage(ExceptionHandler.class, ExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, GitException.class);
		contributeFromPackage(ExceptionHandler.class, UnauthenticatedExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, ConstraintViolationExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, PageExpiredExceptionHandler.class);
		
		bind(UrlManager.class).to(DefaultUrlManager.class);
		bind(CodeCommentEventBroadcaster.class);
		bind(PullRequestEventBroadcaster.class);
		bind(IssueEventBroadcaster.class);
		bind(BuildEventBroadcaster.class);
		bind(UploadItemManager.class).to(DefaultUploadItemManager.class);
		bind(TerminalManager.class).to(DefaultTerminalManager.class);
		
		bind(TaskButton.TaskFutureManager.class);
		
		bind(MainMenuCustomization.class).toInstance(new DefaultMainMenuCustomization());
	}
	
	private void configureBuild() {
		bind(ResourceAllocator.class).to(DefaultResourceAllocator.class);
		bind(AgentManager.class).to(DefaultAgentManager.class);
		bind(AgentTokenManager.class).to(DefaultAgentTokenManager.class);
		bind(AgentAttributeManager.class).to(DefaultAgentAttributeManager.class);
		
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("determine-build-failure-investigator");
				script.setContent(Lists.newArrayList("io.onedev.server.util.script.ScriptContribution.determineBuildFailureInvestigator()"));
				return script;
			}
			
		});
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("get-build-number");
				script.setContent(Lists.newArrayList("io.onedev.server.util.script.ScriptContribution.getBuildNumber()"));
				return script;
			}
			
		});
	}
	
	private void configurePersistence() {
		bind(DataManager.class).to(DefaultDataManager.class);
		
		bind(Session.class).toProvider(SessionProvider.class);
		bind(EntityManager.class).toProvider(SessionProvider.class);
		bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
		bind(EntityManagerFactory.class).toProvider(SessionFactoryProvider.class);
		bind(SessionFactoryManager.class).to(DefaultSessionFactoryManager.class);
		
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    
		bind(Interceptor.class).to(HibernateInterceptor.class);
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("o_"));
		
		bind(SessionManager.class).to(DefaultSessionManager.class);
		bind(TransactionManager.class).to(DefaultTransactionManager.class);
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
								
								return field.getAnnotation(XStreamOmitField.class) == null 
										&& field.getAnnotation(Transient.class) == null 
										&& field.getAnnotation(OneToMany.class) == null 
										&& (field.getAnnotation(OneToOne.class) == null || field.getAnnotation(JoinColumn.class) != null)  
										&& field.getAnnotation(Version.class) == null;
							}
							
							@Override
							public String serializedClass(Class type) {
								if (type == null)
									return super.serializedClass(type);
								else if (type == PersistentBag.class)
									return super.serializedClass(ArrayList.class);
								else if (type.getName().contains("$HibernateProxy$"))
									return StringUtils.substringBefore(type.getName(), "$HibernateProxy$");
								else
									return super.serializedClass(type);
							}
							
						};
					}
					
				};
				xstream.allowTypesByWildcard(new String[] {"io.onedev.**"});				
				
				// register NullConverter as highest; otherwise NPE when unmarshal a map 
				// containing an entry with value set to null.
				xstream.registerConverter(new NullConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new StringConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new VersionedDocumentConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new HibernateProxyConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new CollectionConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new MapConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ISO8601DateConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ISO8601SqlTimestampConverter(), XStream.PRIORITY_VERY_HIGH); 
				xstream.registerConverter(new ReflectionConverter(xstream.getMapper(), xstream.getReflectionProvider()), 
						XStream.PRIORITY_VERY_LOW);
				xstream.autodetectAnnotations(true);
				return xstream;
			}
			
		}).in(Singleton.class);
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		if (Bootstrap.command != null) {
			if (RestoreDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return RestoreDatabase.class;
			else if (ApplyDatabaseConstraints.COMMAND.equals(Bootstrap.command.getName()))
				return ApplyDatabaseConstraints.class;
			else if (BackupDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return BackupDatabase.class;
			else if (CheckDataVersion.COMMAND.equals(Bootstrap.command.getName()))
				return CheckDataVersion.class;
			else if (Upgrade.COMMAND.equals(Bootstrap.command.getName()))
				return Upgrade.class;
			else if (CleanDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return CleanDatabase.class;
			else if (ResetAdminPassword.COMMAND.equals(Bootstrap.command.getName()))
				return ResetAdminPassword.class;
			else	
				throw new RuntimeException("Unrecognized command: " + Bootstrap.command.getName());
		} else {
			return OneDev.class;
		}		
	}

}
