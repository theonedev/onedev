package com.pmease.gitplex.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import com.google.common.collect.Sets;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.PersistListener;
import com.pmease.commons.hibernate.PrefixedNamingStrategy;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.ImplementationProvider;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitplex.core.entity.Config;
import com.pmease.gitplex.core.entity.EntityLocator;
import com.pmease.gitplex.core.entity.persistlistener.ListenerLocator;
import com.pmease.gitplex.core.listener.ConfigListener;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentReplyManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.manager.PullRequestActivityManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.PullRequestVisitManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.SequentialWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.TeamMembershipManager;
import com.pmease.gitplex.core.manager.UserAuthorizationManager;
import com.pmease.gitplex.core.manager.VerificationManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.core.manager.impl.DefaultAccountManager;
import com.pmease.gitplex.core.manager.impl.DefaultTeamAuthorizationManager;
import com.pmease.gitplex.core.manager.impl.DefaultAuxiliaryManager;
import com.pmease.gitplex.core.manager.impl.DefaultBranchWatchManager;
import com.pmease.gitplex.core.manager.impl.DefaultCodeCommentManager;
import com.pmease.gitplex.core.manager.impl.DefaultCodeCommentReplyManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestCommentManager;
import com.pmease.gitplex.core.manager.impl.DefaultConfigManager;
import com.pmease.gitplex.core.manager.impl.DefaultDataManager;
import com.pmease.gitplex.core.manager.impl.DefaultDepotManager;
import com.pmease.gitplex.core.manager.impl.DefaultMailManager;
import com.pmease.gitplex.core.manager.impl.DefaultOrganizationMembershipManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestActivityManager;
import com.pmease.gitplex.core.manager.impl.DefaultNotificationManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestUpdateManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestVisitManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestWatchManager;
import com.pmease.gitplex.core.manager.impl.DefaultReviewInvitationManager;
import com.pmease.gitplex.core.manager.impl.DefaultReviewManager;
import com.pmease.gitplex.core.manager.impl.DefaultSequentialWorkManager;
import com.pmease.gitplex.core.manager.impl.DefaultStorageManager;
import com.pmease.gitplex.core.manager.impl.DefaultTeamManager;
import com.pmease.gitplex.core.manager.impl.DefaultTeamMembershipManager;
import com.pmease.gitplex.core.manager.impl.DefaultUserAuthorizationManager;
import com.pmease.gitplex.core.manager.impl.DefaultVerificationManager;
import com.pmease.gitplex.core.manager.impl.DefaultWorkManager;
import com.pmease.gitplex.core.security.SecurityRealm;
import com.pmease.gitplex.core.setting.SpecifiedGit;
import com.pmease.gitplex.core.setting.SystemGit;
import com.pmease.gitplex.core.util.validation.AccountNameReservation;
import com.pmease.gitplex.core.util.validation.DepotNameReservation;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("g_"));
		
		contribute(ModelProvider.class, new ModelProvider() {

			@Override
			public Collection<Class<? extends AbstractEntity>> getModelClasses() {
				Collection<Class<? extends AbstractEntity>> modelClasses = 
						new HashSet<Class<? extends AbstractEntity>>();
				modelClasses.addAll(ClassUtils.findImplementations(AbstractEntity.class, EntityLocator.class));
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
		bind(VerificationManager.class).to(DefaultVerificationManager.class);
		bind(ReviewInvitationManager.class).to(DefaultReviewInvitationManager.class);
		bind(ReviewManager.class).to(DefaultReviewManager.class);
		bind(MailManager.class).to(DefaultMailManager.class);
		bind(BranchWatchManager.class).to(DefaultBranchWatchManager.class);
		bind(PullRequestWatchManager.class).to(DefaultPullRequestWatchManager.class);
		bind(NotificationManager.class).to(DefaultNotificationManager.class);
		bind(AuxiliaryManager.class).to(DefaultAuxiliaryManager.class);
		bind(WorkManager.class).to(DefaultWorkManager.class);
		bind(SequentialWorkManager.class).to(DefaultSequentialWorkManager.class);
		bind(TeamManager.class).to(DefaultTeamManager.class);
		bind(OrganizationMembershipManager.class).to(DefaultOrganizationMembershipManager.class);
		bind(TeamMembershipManager.class).to(DefaultTeamMembershipManager.class);
		bind(TeamAuthorizationManager.class).to(DefaultTeamAuthorizationManager.class);
		bind(UserAuthorizationManager.class).to(DefaultUserAuthorizationManager.class);
		bind(PullRequestActivityManager.class).to(DefaultPullRequestActivityManager.class);
		bind(PullRequestVisitManager.class).to(DefaultPullRequestVisitManager.class);
		bind(CodeCommentReplyManager.class).to(DefaultCodeCommentReplyManager.class);

		bind(AbstractRealm.class).to(SecurityRealm.class);

		contribute(DepotListener.class, DefaultPullRequestManager.class);
		contribute(DepotListener.class, DefaultAuxiliaryManager.class);
		contribute(DepotListener.class, DefaultTeamAuthorizationManager.class);
		contribute(RefListener.class, DefaultPullRequestManager.class);
		contribute(RefListener.class, DefaultAuxiliaryManager.class);
		contribute(RefListener.class, DefaultDepotManager.class);
		contribute(PullRequestListener.class, DefaultNotificationManager.class);
		contribute(PullRequestListener.class, DefaultPullRequestWatchManager.class);
		contribute(LifecycleListener.class, DefaultPullRequestManager.class);
		contribute(LifecycleListener.class, DefaultAccountManager.class);
		contribute(LifecycleListener.class, DefaultDepotManager.class);
		contribute(LifecycleListener.class, DefaultAuxiliaryManager.class);
		contribute(LifecycleListener.class, DefaultWorkManager.class);
		contribute(LifecycleListener.class, DefaultSequentialWorkManager.class);
		
		contribute(ConfigListener.class, new ConfigListener() {

			@Override
			public void onSave(Config config) {
			}
			
		});
		contributeFromPackage(PersistListener.class, ListenerLocator.class);
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return GitPlex.class;
	}

}
