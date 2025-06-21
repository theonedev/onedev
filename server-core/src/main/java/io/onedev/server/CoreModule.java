package io.onedev.server;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.OsInfo;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.attachment.DefaultAttachmentManager;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.cluster.ClusterResource;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.codequality.LineCoverageContribution;
import io.onedev.server.commandhandler.ApplyDatabaseConstraints;
import io.onedev.server.commandhandler.BackupDatabase;
import io.onedev.server.commandhandler.CheckDataVersion;
import io.onedev.server.commandhandler.CleanDatabase;
import io.onedev.server.commandhandler.ResetAdminPassword;
import io.onedev.server.commandhandler.RestoreDatabase;
import io.onedev.server.commandhandler.Translate;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.data.DataManager;
import io.onedev.server.data.DefaultDataManager;
import io.onedev.server.entitymanager.AccessTokenAuthorizationManager;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentLastUsedDateManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.entitymanager.BaseAuthorizationManager;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildLabelManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.BuildQueryPersonalizationManager;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentMentionManager;
import io.onedev.server.entitymanager.CodeCommentQueryPersonalizationManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.CodeCommentTouchManager;
import io.onedev.server.entitymanager.CommitQueryPersonalizationManager;
import io.onedev.server.entitymanager.DashboardGroupShareManager;
import io.onedev.server.entitymanager.DashboardManager;
import io.onedev.server.entitymanager.DashboardUserShareManager;
import io.onedev.server.entitymanager.DashboardVisitManager;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GitLfsLockManager;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueAuthorizationManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueCommentReactionManager;
import io.onedev.server.entitymanager.IssueCommentRevisionManager;
import io.onedev.server.entitymanager.IssueDescriptionRevisionManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueMentionManager;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.IssueReactionManager;
import io.onedev.server.entitymanager.IssueScheduleManager;
import io.onedev.server.entitymanager.IssueStateHistoryManager;
import io.onedev.server.entitymanager.IssueTouchManager;
import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.entitymanager.JobCacheManager;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.LinkAuthorizationManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.PackBlobManager;
import io.onedev.server.entitymanager.PackBlobReferenceManager;
import io.onedev.server.entitymanager.PackLabelManager;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.entitymanager.PackQueryPersonalizationManager;
import io.onedev.server.entitymanager.PendingSuggestionApplyManager;
import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.entitymanager.ProjectLastEventDateManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestCommentReactionManager;
import io.onedev.server.entitymanager.PullRequestCommentRevisionManager;
import io.onedev.server.entitymanager.PullRequestDescriptionRevisionManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestMentionManager;
import io.onedev.server.entitymanager.PullRequestQueryPersonalizationManager;
import io.onedev.server.entitymanager.PullRequestReactionManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.entitymanager.PullRequestTouchManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.ReviewedDiffManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.StopwatchManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entitymanager.impl.DefaultAccessTokenAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultAccessTokenManager;
import io.onedev.server.entitymanager.impl.DefaultAgentAttributeManager;
import io.onedev.server.entitymanager.impl.DefaultAgentLastUsedDateManager;
import io.onedev.server.entitymanager.impl.DefaultAgentManager;
import io.onedev.server.entitymanager.impl.DefaultAgentTokenManager;
import io.onedev.server.entitymanager.impl.DefaultAlertManager;
import io.onedev.server.entitymanager.impl.DefaultBaseAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultBuildDependenceManager;
import io.onedev.server.entitymanager.impl.DefaultBuildLabelManager;
import io.onedev.server.entitymanager.impl.DefaultBuildManager;
import io.onedev.server.entitymanager.impl.DefaultBuildMetricManager;
import io.onedev.server.entitymanager.impl.DefaultBuildParamManager;
import io.onedev.server.entitymanager.impl.DefaultBuildQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentMentionManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentReplyManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentTouchManager;
import io.onedev.server.entitymanager.impl.DefaultCommitQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultDashboardGroupShareManager;
import io.onedev.server.entitymanager.impl.DefaultDashboardManager;
import io.onedev.server.entitymanager.impl.DefaultDashboardUserShareManager;
import io.onedev.server.entitymanager.impl.DefaultDashboardVisitManager;
import io.onedev.server.entitymanager.impl.DefaultEmailAddressManager;
import io.onedev.server.entitymanager.impl.DefaultGitLfsLockManager;
import io.onedev.server.entitymanager.impl.DefaultGpgKeyManager;
import io.onedev.server.entitymanager.impl.DefaultGroupAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultGroupManager;
import io.onedev.server.entitymanager.impl.DefaultIssueAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultIssueChangeManager;
import io.onedev.server.entitymanager.impl.DefaultIssueCommentManager;
import io.onedev.server.entitymanager.impl.DefaultIssueCommentReactionManager;
import io.onedev.server.entitymanager.impl.DefaultIssueCommentRevisionManager;
import io.onedev.server.entitymanager.impl.DefaultIssueDescriptionRevisionManager;
import io.onedev.server.entitymanager.impl.DefaultIssueFieldManager;
import io.onedev.server.entitymanager.impl.DefaultIssueLinkManager;
import io.onedev.server.entitymanager.impl.DefaultIssueManager;
import io.onedev.server.entitymanager.impl.DefaultIssueMentionManager;
import io.onedev.server.entitymanager.impl.DefaultIssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultIssueReactionManager;
import io.onedev.server.entitymanager.impl.DefaultIssueScheduleManager;
import io.onedev.server.entitymanager.impl.DefaultIssueStateHistoryManager;
import io.onedev.server.entitymanager.impl.DefaultIssueTouchManager;
import io.onedev.server.entitymanager.impl.DefaultIssueVoteManager;
import io.onedev.server.entitymanager.impl.DefaultIssueWatchManager;
import io.onedev.server.entitymanager.impl.DefaultIssueWorkManager;
import io.onedev.server.entitymanager.impl.DefaultIterationManager;
import io.onedev.server.entitymanager.impl.DefaultJobCacheManager;
import io.onedev.server.entitymanager.impl.DefaultLabelSpecManager;
import io.onedev.server.entitymanager.impl.DefaultLinkAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultLinkSpecManager;
import io.onedev.server.entitymanager.impl.DefaultMembershipManager;
import io.onedev.server.entitymanager.impl.DefaultPackBlobManager;
import io.onedev.server.entitymanager.impl.DefaultPackBlobReferenceManager;
import io.onedev.server.entitymanager.impl.DefaultPackLabelManager;
import io.onedev.server.entitymanager.impl.DefaultPackManager;
import io.onedev.server.entitymanager.impl.DefaultPackQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultPendingSuggestionApplyManager;
import io.onedev.server.entitymanager.impl.DefaultProjectLabelManager;
import io.onedev.server.entitymanager.impl.DefaultProjectLastEventDateManager;
import io.onedev.server.entitymanager.impl.DefaultProjectManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestAssignmentManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestChangeManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestCommentManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestCommentReactionManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestCommentRevisionManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestDescriptionRevisionManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestLabelManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestMentionManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestQueryPersonalizationManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestReactionManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestReviewManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestTouchManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestUpdateManager;
import io.onedev.server.entitymanager.impl.DefaultPullRequestWatchManager;
import io.onedev.server.entitymanager.impl.DefaultReviewedDiffManager;
import io.onedev.server.entitymanager.impl.DefaultRoleManager;
import io.onedev.server.entitymanager.impl.DefaultSettingManager;
import io.onedev.server.entitymanager.impl.DefaultSshKeyManager;
import io.onedev.server.entitymanager.impl.DefaultStopwatchManager;
import io.onedev.server.entitymanager.impl.DefaultUserAuthorizationManager;
import io.onedev.server.entitymanager.impl.DefaultUserInvitationManager;
import io.onedev.server.entitymanager.impl.DefaultUserManager;
import io.onedev.server.entityreference.DefaultReferenceChangeManager;
import io.onedev.server.entityreference.ReferenceChangeManager;
import io.onedev.server.event.DefaultListenerRegistry;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.exception.handler.ExceptionHandler;
import io.onedev.server.git.GitFilter;
import io.onedev.server.git.GitLfsFilter;
import io.onedev.server.git.GitLocationProvider;
import io.onedev.server.git.GoGetFilter;
import io.onedev.server.git.SshCommandCreator;
import io.onedev.server.git.hook.GitPostReceiveCallback;
import io.onedev.server.git.hook.GitPreReceiveCallback;
import io.onedev.server.git.hook.GitPreReceiveChecker;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.git.service.DefaultGitService;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.signatureverification.DefaultSignatureVerificationManager;
import io.onedev.server.git.signatureverification.SignatureVerificationManager;
import io.onedev.server.git.signatureverification.SignatureVerifier;
import io.onedev.server.jetty.DefaultJettyManager;
import io.onedev.server.jetty.DefaultSessionDataStoreFactory;
import io.onedev.server.jetty.JettyManager;
import io.onedev.server.job.DefaultJobManager;
import io.onedev.server.job.DefaultResourceAllocator;
import io.onedev.server.job.JobManager;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.log.DefaultLogManager;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.mail.DefaultMailManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.DefaultMarkdownManager;
import io.onedev.server.markdown.HtmlProcessor;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.notification.BuildNotificationManager;
import io.onedev.server.notification.CodeCommentNotificationManager;
import io.onedev.server.notification.CommitNotificationManager;
import io.onedev.server.notification.IssueNotificationManager;
import io.onedev.server.notification.PackNotificationManager;
import io.onedev.server.notification.PullRequestNotificationManager;
import io.onedev.server.notification.WebHookManager;
import io.onedev.server.pack.PackFilter;
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
import io.onedev.server.rest.DefaultServletContainer;
import io.onedev.server.rest.JerseyConfigurator;
import io.onedev.server.rest.ResourceConfigProvider;
import io.onedev.server.rest.WebApplicationExceptionHandler;
import io.onedev.server.rest.resource.ProjectResource;
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
import io.onedev.server.security.realm.GeneralAuthorizingRealm;
import io.onedev.server.ssh.CommandCreator;
import io.onedev.server.ssh.DefaultSshAuthenticator;
import io.onedev.server.ssh.DefaultSshManager;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshManager;
import io.onedev.server.taskschedule.DefaultTaskScheduler;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.updatecheck.DefaultUpdateCheckManager;
import io.onedev.server.updatecheck.UpdateCheckManager;
import io.onedev.server.util.ScriptContribution;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.DefaultBatchWorkManager;
import io.onedev.server.util.concurrent.DefaultWorkExecutor;
import io.onedev.server.util.concurrent.WorkExecutor;
import io.onedev.server.util.jackson.ObjectMapperConfigurator;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.jackson.git.GitObjectMapperConfigurator;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import io.onedev.server.util.oauth.DefaultOAuthTokenManager;
import io.onedev.server.util.oauth.OAuthTokenManager;
import io.onedev.server.util.xstream.CollectionConverter;
import io.onedev.server.util.xstream.HibernateProxyConverter;
import io.onedev.server.util.xstream.MapConverter;
import io.onedev.server.util.xstream.ObjectMapperConverter;
import io.onedev.server.util.xstream.ReflectionConverter;
import io.onedev.server.util.xstream.StringConverter;
import io.onedev.server.util.xstream.VersionedDocumentConverter;
import io.onedev.server.validation.MessageInterpolator;
import io.onedev.server.validation.ValidatorProvider;
import io.onedev.server.web.DefaultUrlManager;
import io.onedev.server.web.DefaultWicketFilter;
import io.onedev.server.web.DefaultWicketServlet;
import io.onedev.server.web.ResourcePackScopeContribution;
import io.onedev.server.web.UrlManager;
import io.onedev.server.web.WebApplication;
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
import io.onedev.server.web.exceptionhandler.PageExpiredExceptionHandler;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import io.onedev.server.web.upload.DefaultUploadManager;
import io.onedev.server.web.upload.UploadManager;
import io.onedev.server.web.websocket.AlertEventBroadcaster;
import io.onedev.server.web.websocket.BuildEventBroadcaster;
import io.onedev.server.web.websocket.CodeCommentEventBroadcaster;
import io.onedev.server.web.websocket.CommitIndexedBroadcaster;
import io.onedev.server.web.websocket.DefaultWebSocketManager;
import io.onedev.server.web.websocket.IssueEventBroadcaster;
import io.onedev.server.web.websocket.PullRequestEventBroadcaster;
import io.onedev.server.web.websocket.WebSocketManager;
import io.onedev.server.xodus.CommitInfoManager;
import io.onedev.server.xodus.DefaultCommitInfoManager;
import io.onedev.server.xodus.DefaultIssueInfoManager;
import io.onedev.server.xodus.DefaultPullRequestInfoManager;
import io.onedev.server.xodus.DefaultVisitInfoManager;
import io.onedev.server.xodus.IssueInfoManager;
import io.onedev.server.xodus.PullRequestInfoManager;
import io.onedev.server.xodus.VisitInfoManager;
import nl.altindag.ssl.SSLFactory;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ListenerRegistry.class).to(DefaultListenerRegistry.class);
		bind(JettyManager.class).to(DefaultJettyManager.class);
		bind(ServletContextHandler.class).toProvider(DefaultJettyManager.class);
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
		
		bind(ValidatorFactory.class).toProvider(() -> {
			Configuration<?> configuration = Validation
					.byDefaultProvider()
					.configure()
					.messageInterpolator(new MessageInterpolator());
			return configuration.buildValidatorFactory();
		}).in(Singleton.class);
		
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);

		configurePersistence();
		configureSecurity();
		configureRestful();
		configureWeb();
		configureGit();
		configureBuild();

		/*
		 * Declare bindings explicitly instead of using ImplementedBy annotation as
		 * HK2 to guice bridge can only search in explicit bindings in Guice   
		 */
		bind(SshAuthenticator.class).to(DefaultSshAuthenticator.class);
		bind(SshManager.class).to(DefaultSshManager.class);
		bind(MarkdownManager.class).to(DefaultMarkdownManager.class);		
		bind(SettingManager.class).to(DefaultSettingManager.class);
		bind(DataManager.class).to(DefaultDataManager.class);
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
		bind(PullRequestCommentManager.class).to(DefaultPullRequestCommentManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(PullRequestManager.class).to(DefaultPullRequestManager.class);
		bind(PullRequestUpdateManager.class).to(DefaultPullRequestUpdateManager.class);
		bind(ProjectManager.class).to(DefaultProjectManager.class);
		bind(ProjectLastEventDateManager.class).to(DefaultProjectLastEventDateManager.class);
		bind(UserInvitationManager.class).to(DefaultUserInvitationManager.class);
		bind(PullRequestReviewManager.class).to(DefaultPullRequestReviewManager.class);
		bind(BuildManager.class).to(DefaultBuildManager.class);
		bind(BuildDependenceManager.class).to(DefaultBuildDependenceManager.class);
		bind(JobManager.class).to(DefaultJobManager.class);		
		bind(JobCacheManager.class).to(DefaultJobCacheManager.class);
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
		bind(PackNotificationManager.class);
		bind(IssueNotificationManager.class);
		bind(CodeCommentNotificationManager.class);
		bind(CodeCommentManager.class).to(DefaultCodeCommentManager.class);
		bind(AccessTokenManager.class).to(DefaultAccessTokenManager.class);
		bind(UserManager.class).to(DefaultUserManager.class);
		bind(IssueWatchManager.class).to(DefaultIssueWatchManager.class);
		bind(IssueChangeManager.class).to(DefaultIssueChangeManager.class);
		bind(IssueVoteManager.class).to(DefaultIssueVoteManager.class);
		bind(IssueWorkManager.class).to(DefaultIssueWorkManager.class);
		bind(IterationManager.class).to(DefaultIterationManager.class);
		bind(IssueCommentManager.class).to(DefaultIssueCommentManager.class);
		bind(IssueQueryPersonalizationManager.class).to(DefaultIssueQueryPersonalizationManager.class);
		bind(PullRequestQueryPersonalizationManager.class).to(DefaultPullRequestQueryPersonalizationManager.class);
		bind(CodeCommentQueryPersonalizationManager.class).to(DefaultCodeCommentQueryPersonalizationManager.class);
		bind(CommitQueryPersonalizationManager.class).to(DefaultCommitQueryPersonalizationManager.class);
		bind(BuildQueryPersonalizationManager.class).to(DefaultBuildQueryPersonalizationManager.class);
		bind(PackQueryPersonalizationManager.class).to(DefaultPackQueryPersonalizationManager.class);
		bind(PullRequestAssignmentManager.class).to(DefaultPullRequestAssignmentManager.class);
		bind(SshKeyManager.class).to(DefaultSshKeyManager.class);
		bind(BuildMetricManager.class).to(DefaultBuildMetricManager.class);
		bind(ReferenceChangeManager.class).to(DefaultReferenceChangeManager.class);
		bind(GitLfsLockManager.class).to(DefaultGitLfsLockManager.class);
		bind(IssueScheduleManager.class).to(DefaultIssueScheduleManager.class);
		bind(LinkSpecManager.class).to(DefaultLinkSpecManager.class);
		bind(IssueLinkManager.class).to(DefaultIssueLinkManager.class);
		bind(IssueStateHistoryManager.class).to(DefaultIssueStateHistoryManager.class);
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
		bind(LabelSpecManager.class).to(DefaultLabelSpecManager.class);
		bind(ProjectLabelManager.class).to(DefaultProjectLabelManager.class);
		bind(BuildLabelManager.class).to(DefaultBuildLabelManager.class);
		bind(PackLabelManager.class).to(DefaultPackLabelManager.class);
		bind(PullRequestLabelManager.class).to(DefaultPullRequestLabelManager.class);
		bind(IssueTouchManager.class).to(DefaultIssueTouchManager.class);
		bind(PullRequestTouchManager.class).to(DefaultPullRequestTouchManager.class);
		bind(CodeCommentTouchManager.class).to(DefaultCodeCommentTouchManager.class);
		bind(AlertManager.class).to(DefaultAlertManager.class);
		bind(UpdateCheckManager.class).to(DefaultUpdateCheckManager.class);
		bind(StopwatchManager.class).to(DefaultStopwatchManager.class);
		bind(PackManager.class).to(DefaultPackManager.class);
		bind(PackBlobManager.class).to(DefaultPackBlobManager.class);
		bind(PackBlobReferenceManager.class).to(DefaultPackBlobReferenceManager.class);
		bind(AccessTokenAuthorizationManager.class).to(DefaultAccessTokenAuthorizationManager.class);
		bind(ReviewedDiffManager.class).to(DefaultReviewedDiffManager.class);
		bind(OAuthTokenManager.class).to(DefaultOAuthTokenManager.class);
		bind(IssueReactionManager.class).to(DefaultIssueReactionManager.class);
		bind(IssueCommentReactionManager.class).to(DefaultIssueCommentReactionManager.class);
		bind(PullRequestReactionManager.class).to(DefaultPullRequestReactionManager.class);
		bind(PullRequestCommentReactionManager.class).to(DefaultPullRequestCommentReactionManager.class);
		bind(IssueCommentRevisionManager.class).to(DefaultIssueCommentRevisionManager.class);
		bind(PullRequestCommentRevisionManager.class).to(DefaultPullRequestCommentRevisionManager.class);
		bind(IssueDescriptionRevisionManager.class).to(DefaultIssueDescriptionRevisionManager.class);
		bind(PullRequestDescriptionRevisionManager.class).to(DefaultPullRequestDescriptionRevisionManager.class);
		bind(BaseAuthorizationManager.class).to(DefaultBaseAuthorizationManager.class);
		
		bind(WebHookManager.class);
		
		contribute(CodePullAuthorizationSource.class, DefaultJobManager.class);
        
		bind(CodeIndexManager.class).to(DefaultCodeIndexManager.class);
		bind(CodeSearchManager.class).to(DefaultCodeSearchManager.class);

		Bootstrap.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<>()) {

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

	    bind(ExecutorService.class).toProvider(() -> Bootstrap.executorService).in(Singleton.class);
	    
	    bind(OsInfo.class).toProvider(() -> ExecutorUtils.getOsInfo()).in(Singleton.class);
	    
	    contributeFromPackage(LogInstruction.class, LogInstruction.class);
	    
	    
		contribute(CodeProblemContribution.class, (build, blobPath, reportName) -> newArrayList());
	    
		contribute(LineCoverageContribution.class, (build, blobPath, reportName) -> new HashMap<>());
		contribute(AdministrationSettingContribution.class, () -> new ArrayList<>());
		contribute(ProjectSettingContribution.class, () -> new ArrayList<>());
		contribute(GitPreReceiveChecker.class, (project, submitter, refName, oldObjectId, newObjectId) -> null);

		bind(PackFilter.class);
	}
	
	private void configureSecurity() {
		contributeFromPackage(Realm.class, GeneralAuthorizingRealm.class);

		bind(ShiroFilterConfiguration.class).to(DefaultShiroFilterConfiguration.class);
		bind(RememberMeManager.class).to(DefaultRememberMeManager.class);
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(BearerAuthenticationFilter.class);
		bind(PasswordService.class).to(DefaultPasswordService.class);
		bind(ShiroFilter.class);
		install(new ShiroAopModule());
        contribute(FilterChainConfigurator.class, filterChainManager -> {
			filterChainManager.createChain("/**/info/refs", "noSessionCreation, authcBasic, authcBearer");
			filterChainManager.createChain("/**/git-upload-pack", "noSessionCreation, authcBasic, authcBearer");
			filterChainManager.createChain("/**/git-receive-pack", "noSessionCreation, authcBasic, authcBearer");
		});
        contributeFromPackage(Authenticator.class, Authenticator.class);
		
		bind(SSLFactory.class).toProvider(() -> KubernetesHelper.buildSSLFactory(Bootstrap.getTrustCertsDir())).in(Singleton.class);
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
		bind(SignatureVerificationManager.class).to(DefaultSignatureVerificationManager.class);
		contribute(CommandCreator.class, SshCommandCreator.class);
		contributeFromPackage(SignatureVerifier.class, SignatureVerifier.class);
	}
	
	private void configureRestful() {
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
		bind(ServletContainer.class).to(DefaultServletContainer.class);
		
		contribute(FilterChainConfigurator.class, filterChainManager -> filterChainManager.createChain("/~api/**", "noSessionCreation, authcBasic, authcBearer"));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.packages(ProjectResource.class.getPackage().getName()));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(ClusterResource.class));
	}

	private void configureWeb() {
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		bind(SessionDataStoreFactory.class).to(DefaultSessionDataStoreFactory.class);

		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		bind(org.apache.wicket.protocol.http.WebApplication.class).to(WebApplication.class);
		bind(Application.class).to(WebApplication.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
				
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRenderer.class, BlobRenderer.class);

		contribute(Extension.class, new EmojiExtension());
		contribute(Extension.class, new SourcePositionTrackExtension());
		
		contributeFromPackage(HtmlProcessor.class, HtmlProcessor.class);

		contribute(ResourcePackScopeContribution.class, () -> newArrayList(WebApplication.class));
		
		contributeFromPackage(ExceptionHandler.class, ExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, ConstraintViolationExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, PageExpiredExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, WebApplicationExceptionHandler.class);
		
		bind(UrlManager.class).to(DefaultUrlManager.class);
		bind(CodeCommentEventBroadcaster.class);
		bind(PullRequestEventBroadcaster.class);
		bind(IssueEventBroadcaster.class);
		bind(BuildEventBroadcaster.class);
		bind(AlertEventBroadcaster.class);
		bind(UploadManager.class).to(DefaultUploadManager.class);
		
		bind(TaskButton.TaskFutureManager.class);
	}
	
	private void configureBuild() {
		bind(ResourceAllocator.class).to(DefaultResourceAllocator.class);
		bind(AgentManager.class).to(DefaultAgentManager.class);
		bind(AgentTokenManager.class).to(DefaultAgentTokenManager.class);
		bind(AgentAttributeManager.class).to(DefaultAgentAttributeManager.class);
		bind(AgentLastUsedDateManager.class).to(DefaultAgentLastUsedDateManager.class);
		
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("determine-build-failure-investigator");
				script.setContent(newArrayList("io.onedev.server.util.ScriptContribution.determineBuildFailureInvestigator()"));
				return script;
			}
			
		});
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("get-build-number");
				script.setContent(newArrayList("io.onedev.server.util.ScriptContribution.getBuildNumber()"));
				return script;
			}
			
		});
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("get-current-user");
				script.setContent(newArrayList("io.onedev.server.util.ScriptContribution.getCurrentUser()"));
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
				xstream.allowTypesByWildcard(new String[] {"**"});				
				
				// register NullConverter as highest; otherwise NPE when unmarshal a map 
				// containing an entry with value set to null.
				xstream.registerConverter(new NullConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new StringConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new VersionedDocumentConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new HibernateProxyConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new CollectionConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new MapConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ObjectMapperConverter(), XStream.PRIORITY_VERY_HIGH);
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
			else if (Translate.COMMAND.equals(Bootstrap.command.getName()))
				return Translate.class;
			else
				throw new RuntimeException("Unrecognized command: " + Bootstrap.command.getName());
		} else {
			return OneDev.class;
		}		
	}

}
