package io.onedev.server;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.lang.annotation.ElementType;
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
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
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
import io.onedev.server.annotation.Shallow;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.attachment.DefaultAttachmentService;
import io.onedev.server.buildspec.BuildSpecSchemaResource;
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
import io.onedev.server.data.DataService;
import io.onedev.server.data.DefaultDataService;
import io.onedev.server.entityreference.DefaultReferenceChangeService;
import io.onedev.server.entityreference.ReferenceChangeService;
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
import io.onedev.server.git.signatureverification.DefaultSignatureVerificationService;
import io.onedev.server.git.signatureverification.SignatureVerificationService;
import io.onedev.server.git.signatureverification.SignatureVerifier;
import io.onedev.server.jetty.DefaultJettyService;
import io.onedev.server.jetty.DefaultSessionDataStoreFactory;
import io.onedev.server.jetty.JettyService;
import io.onedev.server.job.DefaultJobService;
import io.onedev.server.job.DefaultResourceAllocator;
import io.onedev.server.job.JobService;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.log.DefaultLogService;
import io.onedev.server.job.log.LogService;
import io.onedev.server.mail.DefaultMailService;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.DefaultMarkdownService;
import io.onedev.server.markdown.HtmlProcessor;
import io.onedev.server.markdown.MarkdownService;
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
import io.onedev.server.persistence.DefaultIdService;
import io.onedev.server.persistence.DefaultSessionFactoryService;
import io.onedev.server.persistence.DefaultSessionService;
import io.onedev.server.persistence.DefaultTransactionService;
import io.onedev.server.persistence.HibernateInterceptor;
import io.onedev.server.persistence.IdService;
import io.onedev.server.persistence.PersistListener;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.persistence.SessionFactoryProvider;
import io.onedev.server.persistence.SessionInterceptor;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.SessionProvider;
import io.onedev.server.persistence.TransactionInterceptor;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.DefaultDao;
import io.onedev.server.persistence.exception.ConstraintViolationExceptionHandler;
import io.onedev.server.rest.DefaultServletContainer;
import io.onedev.server.rest.JerseyConfigurator;
import io.onedev.server.rest.ResourceConfigProvider;
import io.onedev.server.rest.WebApplicationExceptionHandler;
import io.onedev.server.rest.resource.McpHelperResource;
import io.onedev.server.rest.resource.ProjectResource;
import io.onedev.server.search.code.CodeIndexService;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.DefaultCodeIndexService;
import io.onedev.server.search.code.DefaultCodeSearchService;
import io.onedev.server.search.entitytext.CodeCommentTextService;
import io.onedev.server.search.entitytext.DefaultCodeCommentTextService;
import io.onedev.server.search.entitytext.DefaultIssueTextService;
import io.onedev.server.search.entitytext.DefaultPullRequestTextService;
import io.onedev.server.search.entitytext.IssueTextService;
import io.onedev.server.search.entitytext.PullRequestTextService;
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
import io.onedev.server.service.AccessTokenAuthorizationService;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AgentAttributeService;
import io.onedev.server.service.AgentLastUsedDateService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.AgentTokenService;
import io.onedev.server.service.AlertService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.BuildDependenceService;
import io.onedev.server.service.BuildLabelService;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.service.BuildParamService;
import io.onedev.server.service.BuildQueryPersonalizationService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.CodeCommentMentionService;
import io.onedev.server.service.CodeCommentQueryPersonalizationService;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.CodeCommentTouchService;
import io.onedev.server.service.CommitQueryPersonalizationService;
import io.onedev.server.service.DashboardGroupShareService;
import io.onedev.server.service.DashboardService;
import io.onedev.server.service.DashboardUserShareService;
import io.onedev.server.service.DashboardVisitService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.GitLfsLockService;
import io.onedev.server.service.GpgKeyService;
import io.onedev.server.service.GroupAuthorizationService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueCommentReactionService;
import io.onedev.server.service.IssueCommentRevisionService;
import io.onedev.server.service.IssueDescriptionRevisionService;
import io.onedev.server.service.IssueFieldService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueMentionService;
import io.onedev.server.service.IssueQueryPersonalizationService;
import io.onedev.server.service.IssueReactionService;
import io.onedev.server.service.IssueScheduleService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueStateHistoryService;
import io.onedev.server.service.IssueTouchService;
import io.onedev.server.service.IssueVoteService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkAuthorizationService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.MembershipService;
import io.onedev.server.service.PackBlobReferenceService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackLabelService;
import io.onedev.server.service.PackQueryPersonalizationService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.PendingSuggestionApplyService;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.service.ProjectLastEventDateService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestAssignmentService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestCommentReactionService;
import io.onedev.server.service.PullRequestCommentRevisionService;
import io.onedev.server.service.PullRequestDescriptionRevisionService;
import io.onedev.server.service.PullRequestLabelService;
import io.onedev.server.service.PullRequestMentionService;
import io.onedev.server.service.PullRequestQueryPersonalizationService;
import io.onedev.server.service.PullRequestReactionService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestTouchService;
import io.onedev.server.service.PullRequestUpdateService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.ReviewedDiffService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.SshKeyService;
import io.onedev.server.service.SsoAccountService;
import io.onedev.server.service.SsoProviderService;
import io.onedev.server.service.StopwatchService;
import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.service.UserInvitationService;
import io.onedev.server.service.UserService;
import io.onedev.server.service.impl.DefaultAccessTokenAuthorizationService;
import io.onedev.server.service.impl.DefaultAccessTokenService;
import io.onedev.server.service.impl.DefaultAgentAttributeService;
import io.onedev.server.service.impl.DefaultAgentLastUsedDateService;
import io.onedev.server.service.impl.DefaultAgentService;
import io.onedev.server.service.impl.DefaultAgentTokenService;
import io.onedev.server.service.impl.DefaultAlertService;
import io.onedev.server.service.impl.DefaultBaseAuthorizationService;
import io.onedev.server.service.impl.DefaultBuildDependenceService;
import io.onedev.server.service.impl.DefaultBuildLabelService;
import io.onedev.server.service.impl.DefaultBuildMetricService;
import io.onedev.server.service.impl.DefaultBuildParamService;
import io.onedev.server.service.impl.DefaultBuildQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultBuildService;
import io.onedev.server.service.impl.DefaultCodeCommentService;
import io.onedev.server.service.impl.DefaultCodeCommentMentionService;
import io.onedev.server.service.impl.DefaultCodeCommentQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultCodeCommentReplyService;
import io.onedev.server.service.impl.DefaultCodeCommentStatusChangeService;
import io.onedev.server.service.impl.DefaultCodeCommentTouchService;
import io.onedev.server.service.impl.DefaultCommitQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultDashboardGroupShareService;
import io.onedev.server.service.impl.DefaultDashboardService;
import io.onedev.server.service.impl.DefaultDashboardUserShareService;
import io.onedev.server.service.impl.DefaultDashboardVisitService;
import io.onedev.server.service.impl.DefaultEmailAddressService;
import io.onedev.server.service.impl.DefaultGitLfsLockService;
import io.onedev.server.service.impl.DefaultGpgKeyService;
import io.onedev.server.service.impl.DefaultGroupAuthorizationService;
import io.onedev.server.service.impl.DefaultGroupService;
import io.onedev.server.service.impl.DefaultIssueAuthorizationService;
import io.onedev.server.service.impl.DefaultIssueChangeService;
import io.onedev.server.service.impl.DefaultIssueCommentService;
import io.onedev.server.service.impl.DefaultIssueCommentReactionService;
import io.onedev.server.service.impl.DefaultIssueCommentRevisionService;
import io.onedev.server.service.impl.DefaultIssueDescriptionRevisionService;
import io.onedev.server.service.impl.DefaultIssueFieldService;
import io.onedev.server.service.impl.DefaultIssueLinkService;
import io.onedev.server.service.impl.DefaultIssueMentionService;
import io.onedev.server.service.impl.DefaultIssueQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultIssueReactionService;
import io.onedev.server.service.impl.DefaultIssueScheduleService;
import io.onedev.server.service.impl.DefaultIssueService;
import io.onedev.server.service.impl.DefaultIssueStateHistoryService;
import io.onedev.server.service.impl.DefaultIssueTouchService;
import io.onedev.server.service.impl.DefaultIssueVoteService;
import io.onedev.server.service.impl.DefaultIssueWatchService;
import io.onedev.server.service.impl.DefaultIssueWorkService;
import io.onedev.server.service.impl.DefaultIterationService;
import io.onedev.server.service.impl.DefaultJobCacheService;
import io.onedev.server.service.impl.DefaultLabelSpecService;
import io.onedev.server.service.impl.DefaultLinkAuthorizationService;
import io.onedev.server.service.impl.DefaultLinkSpecService;
import io.onedev.server.service.impl.DefaultMembershipService;
import io.onedev.server.service.impl.DefaultPackBlobReferenceService;
import io.onedev.server.service.impl.DefaultPackBlobService;
import io.onedev.server.service.impl.DefaultPackLabelService;
import io.onedev.server.service.impl.DefaultPackQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultPackService;
import io.onedev.server.service.impl.DefaultPendingSuggestionApplyService;
import io.onedev.server.service.impl.DefaultProjectLabelService;
import io.onedev.server.service.impl.DefaultProjectLastEventDateService;
import io.onedev.server.service.impl.DefaultProjectService;
import io.onedev.server.service.impl.DefaultPullRequestAssignmentService;
import io.onedev.server.service.impl.DefaultPullRequestChangeService;
import io.onedev.server.service.impl.DefaultPullRequestCommentService;
import io.onedev.server.service.impl.DefaultPullRequestCommentReactionService;
import io.onedev.server.service.impl.DefaultPullRequestCommentRevisionService;
import io.onedev.server.service.impl.DefaultPullRequestDescriptionRevisionService;
import io.onedev.server.service.impl.DefaultPullRequestLabelService;
import io.onedev.server.service.impl.DefaultPullRequestMentionService;
import io.onedev.server.service.impl.DefaultPullRequestQueryPersonalizationService;
import io.onedev.server.service.impl.DefaultPullRequestReactionService;
import io.onedev.server.service.impl.DefaultPullRequestReviewService;
import io.onedev.server.service.impl.DefaultPullRequestService;
import io.onedev.server.service.impl.DefaultPullRequestTouchService;
import io.onedev.server.service.impl.DefaultPullRequestUpdateService;
import io.onedev.server.service.impl.DefaultPullRequestWatchService;
import io.onedev.server.service.impl.DefaultReviewedDiffService;
import io.onedev.server.service.impl.DefaultRoleService;
import io.onedev.server.service.impl.DefaultSettingService;
import io.onedev.server.service.impl.DefaultSshKeyService;
import io.onedev.server.service.impl.DefaultSsoAccountService;
import io.onedev.server.service.impl.DefaultSsoProviderService;
import io.onedev.server.service.impl.DefaultStopwatchService;
import io.onedev.server.service.impl.DefaultUserAuthorizationService;
import io.onedev.server.service.impl.DefaultUserInvitationService;
import io.onedev.server.service.impl.DefaultUserService;
import io.onedev.server.ssh.CommandCreator;
import io.onedev.server.ssh.DefaultSshAuthenticator;
import io.onedev.server.ssh.DefaultSshService;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshService;
import io.onedev.server.taskschedule.DefaultTaskScheduler;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.updatecheck.DefaultUpdateCheckService;
import io.onedev.server.updatecheck.UpdateCheckService;
import io.onedev.server.util.ScriptContribution;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.DefaultBatchWorkExecutionService;
import io.onedev.server.util.concurrent.DefaultWorkExecutionService;
import io.onedev.server.util.concurrent.WorkExecutionService;
import io.onedev.server.util.jackson.ObjectMapperConfigurator;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.jackson.git.GitObjectMapperConfigurator;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import io.onedev.server.util.oauth.DefaultOAuthTokenService;
import io.onedev.server.util.oauth.OAuthTokenService;
import io.onedev.server.util.xstream.CollectionConverter;
import io.onedev.server.util.xstream.HibernateProxyConverter;
import io.onedev.server.util.xstream.MapConverter;
import io.onedev.server.util.xstream.ObjectMapperConverter;
import io.onedev.server.util.xstream.ReflectionConverter;
import io.onedev.server.util.xstream.StringConverter;
import io.onedev.server.util.xstream.VersionedDocumentConverter;
import io.onedev.server.validation.MessageInterpolator;
import io.onedev.server.validation.ShallowValidatorProvider;
import io.onedev.server.validation.ValidatorProvider;
import io.onedev.server.web.DefaultUrlService;
import io.onedev.server.web.DefaultWicketFilter;
import io.onedev.server.web.DefaultWicketServlet;
import io.onedev.server.web.ResourcePackScopeContribution;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.WebApplication;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.avatar.DefaultAvatarService;
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
import io.onedev.server.web.upload.DefaultUploadService;
import io.onedev.server.web.upload.UploadService;
import io.onedev.server.web.websocket.AlertEventBroadcaster;
import io.onedev.server.web.websocket.BuildEventBroadcaster;
import io.onedev.server.web.websocket.CodeCommentEventBroadcaster;
import io.onedev.server.web.websocket.CommitIndexedBroadcaster;
import io.onedev.server.web.websocket.DefaultWebSocketService;
import io.onedev.server.web.websocket.IssueEventBroadcaster;
import io.onedev.server.web.websocket.PullRequestEventBroadcaster;
import io.onedev.server.web.websocket.WebSocketService;
import io.onedev.server.xodus.CommitInfoService;
import io.onedev.server.xodus.DefaultCommitInfoService;
import io.onedev.server.xodus.DefaultIssueInfoService;
import io.onedev.server.xodus.DefaultPullRequestInfoService;
import io.onedev.server.xodus.DefaultVisitInfoService;
import io.onedev.server.xodus.IssueInfoService;
import io.onedev.server.xodus.PullRequestInfoService;
import io.onedev.server.xodus.VisitInfoService;
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
		bind(JettyService.class).to(DefaultJettyService.class);
		bind(ServletContextHandler.class).toProvider(DefaultJettyService.class);
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
		
		bind(ValidatorFactory.class).toProvider(() -> {
			Configuration<?> configuration = Validation
					.byDefaultProvider()
					.configure()
					.messageInterpolator(new MessageInterpolator());
			return configuration.buildValidatorFactory();
		}).in(Singleton.class);

		bind(ValidatorFactory.class).annotatedWith(Shallow.class).toProvider(() -> {
			Configuration<?> configuration = Validation
					.byDefaultProvider()
					.configure()
					.traversableResolver(new TraversableResolver() {

						@Override
						public boolean isReachable(Object traversableObject, Node traversableProperty,
								Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
							return true;
						}
	
						@Override
						public boolean isCascadable(Object traversableObject, Node traversableProperty,
								Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
							return false;
						}
					})					
					.messageInterpolator(new MessageInterpolator());
			return configuration.buildValidatorFactory();
		}).in(Singleton.class);
		
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);
		bind(Validator.class).annotatedWith(Shallow.class).toProvider(ShallowValidatorProvider.class).in(Singleton.class);

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
		bind(SshService.class).to(DefaultSshService.class);
		bind(MarkdownService.class).to(DefaultMarkdownService.class);
		bind(SettingService.class).to(DefaultSettingService.class);
		bind(DataService.class).to(DefaultDataService.class);
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
		bind(PullRequestCommentService.class).to(DefaultPullRequestCommentService.class);
		bind(CodeCommentService.class).to(DefaultCodeCommentService.class);
		bind(PullRequestService.class).to(DefaultPullRequestService.class);
		bind(PullRequestUpdateService.class).to(DefaultPullRequestUpdateService.class);
		bind(ProjectService.class).to(DefaultProjectService.class);
		bind(ProjectLastEventDateService.class).to(DefaultProjectLastEventDateService.class);
		bind(UserInvitationService.class).to(DefaultUserInvitationService.class);
		bind(PullRequestReviewService.class).to(DefaultPullRequestReviewService.class);
		bind(BuildService.class).to(DefaultBuildService.class);
		bind(BuildDependenceService.class).to(DefaultBuildDependenceService.class);
		bind(JobService.class).to(DefaultJobService.class);
		bind(JobCacheService.class).to(DefaultJobCacheService.class);
		bind(LogService.class).to(DefaultLogService.class);
		bind(MailService.class).to(DefaultMailService.class);
		bind(IssueService.class).to(DefaultIssueService.class);
		bind(IssueFieldService.class).to(DefaultIssueFieldService.class);
		bind(BuildParamService.class).to(DefaultBuildParamService.class);
		bind(UserAuthorizationService.class).to(DefaultUserAuthorizationService.class);
		bind(GroupAuthorizationService.class).to(DefaultGroupAuthorizationService.class);
		bind(PullRequestWatchService.class).to(DefaultPullRequestWatchService.class);
		bind(RoleService.class).to(DefaultRoleService.class);
		bind(CommitInfoService.class).to(DefaultCommitInfoService.class);
		bind(IssueInfoService.class).to(DefaultIssueInfoService.class);
		bind(VisitInfoService.class).to(DefaultVisitInfoService.class);
		bind(BatchWorkExecutionService.class).to(DefaultBatchWorkExecutionService.class);
		bind(WorkExecutionService.class).to(DefaultWorkExecutionService.class);
		bind(GroupService.class).to(DefaultGroupService.class);
		bind(IssueMentionService.class).to(DefaultIssueMentionService.class);
		bind(PullRequestMentionService.class).to(DefaultPullRequestMentionService.class);
		bind(CodeCommentMentionService.class).to(DefaultCodeCommentMentionService.class);
		bind(MembershipService.class).to(DefaultMembershipService.class);
		bind(PullRequestChangeService.class).to(DefaultPullRequestChangeService.class);
		bind(CodeCommentReplyService.class).to(DefaultCodeCommentReplyService.class);
		bind(CodeCommentStatusChangeService.class).to(DefaultCodeCommentStatusChangeService.class);
		bind(AttachmentService.class).to(DefaultAttachmentService.class);
		bind(PullRequestInfoService.class).to(DefaultPullRequestInfoService.class);
		bind(PullRequestNotificationManager.class);
		bind(CommitNotificationManager.class);
		bind(BuildNotificationManager.class);
		bind(PackNotificationManager.class);
		bind(IssueNotificationManager.class);
		bind(CodeCommentNotificationManager.class);
		bind(CodeCommentService.class).to(DefaultCodeCommentService.class);
		bind(AccessTokenService.class).to(DefaultAccessTokenService.class);
		bind(UserService.class).to(DefaultUserService.class);
		bind(IssueWatchService.class).to(DefaultIssueWatchService.class);
		bind(IssueChangeService.class).to(DefaultIssueChangeService.class);
		bind(IssueVoteService.class).to(DefaultIssueVoteService.class);
		bind(IssueWorkService.class).to(DefaultIssueWorkService.class);
		bind(IterationService.class).to(DefaultIterationService.class);
		bind(IssueCommentService.class).to(DefaultIssueCommentService.class);
		bind(IssueQueryPersonalizationService.class).to(DefaultIssueQueryPersonalizationService.class);
		bind(PullRequestQueryPersonalizationService.class).to(DefaultPullRequestQueryPersonalizationService.class);
		bind(CodeCommentQueryPersonalizationService.class).to(DefaultCodeCommentQueryPersonalizationService.class);
		bind(CommitQueryPersonalizationService.class).to(DefaultCommitQueryPersonalizationService.class);
		bind(BuildQueryPersonalizationService.class).to(DefaultBuildQueryPersonalizationService.class);
		bind(PackQueryPersonalizationService.class).to(DefaultPackQueryPersonalizationService.class);
		bind(PullRequestAssignmentService.class).to(DefaultPullRequestAssignmentService.class);
		bind(SshKeyService.class).to(DefaultSshKeyService.class);
		bind(BuildMetricService.class).to(DefaultBuildMetricService.class);
		bind(ReferenceChangeService.class).to(DefaultReferenceChangeService.class);
		bind(GitLfsLockService.class).to(DefaultGitLfsLockService.class);
		bind(IssueScheduleService.class).to(DefaultIssueScheduleService.class);
		bind(LinkSpecService.class).to(DefaultLinkSpecService.class);
		bind(IssueLinkService.class).to(DefaultIssueLinkService.class);
		bind(IssueStateHistoryService.class).to(DefaultIssueStateHistoryService.class);
		bind(LinkAuthorizationService.class).to(DefaultLinkAuthorizationService.class);
		bind(EmailAddressService.class).to(DefaultEmailAddressService.class);
		bind(GpgKeyService.class).to(DefaultGpgKeyService.class);
		bind(IssueTextService.class).to(DefaultIssueTextService.class);
		bind(PullRequestTextService.class).to(DefaultPullRequestTextService.class);
		bind(CodeCommentTextService.class).to(DefaultCodeCommentTextService.class);
		bind(PendingSuggestionApplyService.class).to(DefaultPendingSuggestionApplyService.class);
		bind(IssueAuthorizationService.class).to(DefaultIssueAuthorizationService.class);
		bind(DashboardService.class).to(DefaultDashboardService.class);
		bind(DashboardUserShareService.class).to(DefaultDashboardUserShareService.class);
		bind(DashboardGroupShareService.class).to(DefaultDashboardGroupShareService.class);
		bind(DashboardVisitService.class).to(DefaultDashboardVisitService.class);
		bind(LabelSpecService.class).to(DefaultLabelSpecService.class);
		bind(ProjectLabelService.class).to(DefaultProjectLabelService.class);
		bind(BuildLabelService.class).to(DefaultBuildLabelService.class);
		bind(PackLabelService.class).to(DefaultPackLabelService.class);
		bind(PullRequestLabelService.class).to(DefaultPullRequestLabelService.class);
		bind(IssueTouchService.class).to(DefaultIssueTouchService.class);
		bind(PullRequestTouchService.class).to(DefaultPullRequestTouchService.class);
		bind(CodeCommentTouchService.class).to(DefaultCodeCommentTouchService.class);
		bind(AlertService.class).to(DefaultAlertService.class);
		bind(UpdateCheckService.class).to(DefaultUpdateCheckService.class);
		bind(StopwatchService.class).to(DefaultStopwatchService.class);
		bind(PackService.class).to(DefaultPackService.class);
		bind(PackBlobService.class).to(DefaultPackBlobService.class);
		bind(PackBlobReferenceService.class).to(DefaultPackBlobReferenceService.class);
		bind(AccessTokenAuthorizationService.class).to(DefaultAccessTokenAuthorizationService.class);
		bind(ReviewedDiffService.class).to(DefaultReviewedDiffService.class);
		bind(OAuthTokenService.class).to(DefaultOAuthTokenService.class);
		bind(IssueReactionService.class).to(DefaultIssueReactionService.class);
		bind(IssueCommentReactionService.class).to(DefaultIssueCommentReactionService.class);
		bind(PullRequestReactionService.class).to(DefaultPullRequestReactionService.class);
		bind(PullRequestCommentReactionService.class).to(DefaultPullRequestCommentReactionService.class);
		bind(IssueCommentRevisionService.class).to(DefaultIssueCommentRevisionService.class);
		bind(PullRequestCommentRevisionService.class).to(DefaultPullRequestCommentRevisionService.class);
		bind(IssueDescriptionRevisionService.class).to(DefaultIssueDescriptionRevisionService.class);
		bind(PullRequestDescriptionRevisionService.class).to(DefaultPullRequestDescriptionRevisionService.class);
		bind(SsoProviderService.class).to(DefaultSsoProviderService.class);
		bind(SsoAccountService.class).to(DefaultSsoAccountService.class);
		bind(BaseAuthorizationService.class).to(DefaultBaseAuthorizationService.class);
		
		bind(WebHookManager.class);
		
		contribute(CodePullAuthorizationSource.class, DefaultJobService.class);
        
		bind(CodeIndexService.class).to(DefaultCodeIndexService.class);
		bind(CodeSearchService.class).to(DefaultCodeSearchService.class);

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
		bind(SignatureVerificationService.class).to(DefaultSignatureVerificationService.class);
		contribute(CommandCreator.class, SshCommandCreator.class);
		contributeFromPackage(SignatureVerifier.class, SignatureVerifier.class);
	}
	
	private void configureRestful() {
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
		bind(ServletContainer.class).to(DefaultServletContainer.class);
		
		contribute(FilterChainConfigurator.class, filterChainManager -> filterChainManager.createChain("/~api/**", "noSessionCreation, authcBasic, authcBearer"));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.packages(ProjectResource.class.getPackage().getName()));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(ClusterResource.class));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(McpHelperResource.class));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(BuildSpecSchemaResource.class));
	}

	private void configureWeb() {
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketService.class).to(DefaultWebSocketService.class);
		bind(SessionDataStoreFactory.class).to(DefaultSessionDataStoreFactory.class);

		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		bind(org.apache.wicket.protocol.http.WebApplication.class).to(WebApplication.class);
		bind(Application.class).to(WebApplication.class);
		bind(AvatarService.class).to(DefaultAvatarService.class);
		bind(WebSocketService.class).to(DefaultWebSocketService.class);
		
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
		
		bind(UrlService.class).to(DefaultUrlService.class);
		bind(CodeCommentEventBroadcaster.class);
		bind(PullRequestEventBroadcaster.class);
		bind(IssueEventBroadcaster.class);
		bind(BuildEventBroadcaster.class);
		bind(AlertEventBroadcaster.class);
		bind(UploadService.class).to(DefaultUploadService.class);
		
		bind(TaskButton.TaskFutureManager.class);
	}
	
	private void configureBuild() {
		bind(ResourceAllocator.class).to(DefaultResourceAllocator.class);
		bind(AgentService.class).to(DefaultAgentService.class);
		bind(AgentTokenService.class).to(DefaultAgentTokenService.class);
		bind(AgentAttributeService.class).to(DefaultAgentAttributeService.class);
		bind(AgentLastUsedDateService.class).to(DefaultAgentLastUsedDateService.class);
		
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
		bind(DataService.class).to(DefaultDataService.class);
		
		bind(Session.class).toProvider(SessionProvider.class);
		bind(EntityManager.class).toProvider(SessionProvider.class);
		bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
		bind(EntityManagerFactory.class).toProvider(SessionFactoryProvider.class);
		bind(SessionFactoryService.class).to(DefaultSessionFactoryService.class);
		
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    
		bind(Interceptor.class).to(HibernateInterceptor.class);
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("o_"));
		
		bind(SessionService.class).to(DefaultSessionService.class);
		bind(TransactionService.class).to(DefaultTransactionService.class);
		bind(IdService.class).to(DefaultIdService.class);
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
