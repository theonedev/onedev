package com.gitplex.server.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import com.gitplex.calla.bootstrap.Bootstrap;
import com.gitplex.calla.loader.AbstractPlugin;
import com.gitplex.calla.loader.AbstractPluginModule;
import com.gitplex.calla.loader.ImplementationProvider;
import com.gitplex.calla.loader.LoaderUtils;
import com.gitplex.commons.git.GitConfig;
import com.gitplex.commons.hibernate.AbstractEntity;
import com.gitplex.commons.hibernate.EntityValidator;
import com.gitplex.commons.hibernate.ModelProvider;
import com.gitplex.commons.hibernate.PersistManager;
import com.gitplex.commons.hibernate.PrefixedNamingStrategy;
import com.gitplex.commons.hibernate.migration.Migrator;
import com.gitplex.commons.jetty.ServletConfigurator;
import com.gitplex.commons.shiro.AbstractRealm;
import com.gitplex.server.core.commitmessagetransform.CommitMessageTransformer;
import com.gitplex.server.core.commitmessagetransform.PatternCommitMessageTransformer;
import com.gitplex.server.core.entity.EntityLocator;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.manager.AttachmentManager;
import com.gitplex.server.core.manager.BatchWorkManager;
import com.gitplex.server.core.manager.BranchWatchManager;
import com.gitplex.server.core.manager.CodeCommentInfoManager;
import com.gitplex.server.core.manager.CodeCommentManager;
import com.gitplex.server.core.manager.CodeCommentRelationManager;
import com.gitplex.server.core.manager.CodeCommentReplyManager;
import com.gitplex.server.core.manager.CodeCommentStatusChangeManager;
import com.gitplex.server.core.manager.CommitInfoManager;
import com.gitplex.server.core.manager.ConfigManager;
import com.gitplex.server.core.manager.DataManager;
import com.gitplex.server.core.manager.DepotManager;
import com.gitplex.server.core.manager.MailManager;
import com.gitplex.server.core.manager.OrganizationMembershipManager;
import com.gitplex.server.core.manager.PullRequestCommentManager;
import com.gitplex.server.core.manager.PullRequestInfoManager;
import com.gitplex.server.core.manager.PullRequestManager;
import com.gitplex.server.core.manager.PullRequestReferenceManager;
import com.gitplex.server.core.manager.PullRequestReviewInvitationManager;
import com.gitplex.server.core.manager.PullRequestReviewManager;
import com.gitplex.server.core.manager.PullRequestStatusChangeManager;
import com.gitplex.server.core.manager.PullRequestTaskManager;
import com.gitplex.server.core.manager.PullRequestUpdateManager;
import com.gitplex.server.core.manager.PullRequestVerificationManager;
import com.gitplex.server.core.manager.PullRequestWatchManager;
import com.gitplex.server.core.manager.StorageManager;
import com.gitplex.server.core.manager.TeamAuthorizationManager;
import com.gitplex.server.core.manager.TeamManager;
import com.gitplex.server.core.manager.TeamMembershipManager;
import com.gitplex.server.core.manager.UserAuthorizationManager;
import com.gitplex.server.core.manager.VisitInfoManager;
import com.gitplex.server.core.manager.WorkExecutor;
import com.gitplex.server.core.manager.impl.DefaultAccountManager;
import com.gitplex.server.core.manager.impl.DefaultAttachmentManager;
import com.gitplex.server.core.manager.impl.DefaultBatchWorkManager;
import com.gitplex.server.core.manager.impl.DefaultBranchWatchManager;
import com.gitplex.server.core.manager.impl.DefaultCodeCommentInfoManager;
import com.gitplex.server.core.manager.impl.DefaultCodeCommentManager;
import com.gitplex.server.core.manager.impl.DefaultCodeCommentRelationManager;
import com.gitplex.server.core.manager.impl.DefaultCodeCommentReplyManager;
import com.gitplex.server.core.manager.impl.DefaultCodeCommentStatusChangeManager;
import com.gitplex.server.core.manager.impl.DefaultCommitInfoManager;
import com.gitplex.server.core.manager.impl.DefaultConfigManager;
import com.gitplex.server.core.manager.impl.DefaultDataManager;
import com.gitplex.server.core.manager.impl.DefaultDepotManager;
import com.gitplex.server.core.manager.impl.DefaultMailManager;
import com.gitplex.server.core.manager.impl.DefaultOrganizationMembershipManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestCommentManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestInfoManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestReferenceManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestReviewInvitationManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestReviewManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestStatusChangeManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestTaskManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestUpdateManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestVerificationManager;
import com.gitplex.server.core.manager.impl.DefaultPullRequestWatchManager;
import com.gitplex.server.core.manager.impl.DefaultStorageManager;
import com.gitplex.server.core.manager.impl.DefaultTeamAuthorizationManager;
import com.gitplex.server.core.manager.impl.DefaultTeamManager;
import com.gitplex.server.core.manager.impl.DefaultTeamMembershipManager;
import com.gitplex.server.core.manager.impl.DefaultUserAuthorizationManager;
import com.gitplex.server.core.manager.impl.DefaultVisitInfoManager;
import com.gitplex.server.core.manager.impl.DefaultWorkExecutor;
import com.gitplex.server.core.migration.DatabaseMigrator;
import com.gitplex.server.core.security.SecurityRealm;
import com.gitplex.server.core.setting.SpecifiedGit;
import com.gitplex.server.core.setting.SystemGit;
import com.gitplex.server.core.util.validation.AccountNameReservation;
import com.gitplex.server.core.util.validation.DepotNameReservation;
import com.google.common.collect.Sets;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(Migrator.class).to(DatabaseMigrator.class);
		
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("g_"));
		
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
		
		contribute(ServletConfigurator.class, CoreServletConfigurator.class);
		
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
		
		contribute(CommitMessageTransformer.class, PatternCommitMessageTransformer.class);
		
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

		bind(AbstractRealm.class).to(SecurityRealm.class);
		
		bind(EntityValidator.class).to(CoreEntityValidator.class);
		
		if (Bootstrap.command != null 
				&& Bootstrap.command.getName().equals("reset_admin_password")) {
			bind(PersistManager.class).to(ResetAdminPasswordCommand.class);
		}
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return GitPlex.class;
	}

}
