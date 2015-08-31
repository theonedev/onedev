package com.pmease.gitplex.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.cfg.NamingStrategy;

import com.google.common.collect.Sets;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.PrefixedNamingStrategy;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.ImplementationProvider;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitplex.core.listeners.ConfigListener;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.manager.CommentReplyManager;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.manager.VerificationManager;
import com.pmease.gitplex.core.manager.impl.DefaultAuthorizationManager;
import com.pmease.gitplex.core.manager.impl.DefaultBranchWatchManager;
import com.pmease.gitplex.core.manager.impl.DefaultCommentManager;
import com.pmease.gitplex.core.manager.impl.DefaultCommentReplyManager;
import com.pmease.gitplex.core.manager.impl.DefaultConfigManager;
import com.pmease.gitplex.core.manager.impl.DefaultDataManager;
import com.pmease.gitplex.core.manager.impl.DefaultMailManager;
import com.pmease.gitplex.core.manager.impl.DefaultNotificationManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestUpdateManager;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestWatchManager;
import com.pmease.gitplex.core.manager.impl.DefaultRepositoryManager;
import com.pmease.gitplex.core.manager.impl.DefaultReviewInvitationManager;
import com.pmease.gitplex.core.manager.impl.DefaultReviewManager;
import com.pmease.gitplex.core.manager.impl.DefaultStorageManager;
import com.pmease.gitplex.core.manager.impl.DefaultTeamManager;
import com.pmease.gitplex.core.manager.impl.DefaultUserManager;
import com.pmease.gitplex.core.manager.impl.DefaultVerificationManager;
import com.pmease.gitplex.core.model.Config;
import com.pmease.gitplex.core.model.ModelLocator;
import com.pmease.gitplex.core.security.SecurityRealm;
import com.pmease.gitplex.core.setting.SpecifiedGit;
import com.pmease.gitplex.core.setting.SystemGit;
import com.pmease.gitplex.core.validation.RepositoryNameReservation;
import com.pmease.gitplex.core.validation.UserNameReservation;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(NamingStrategy.class).toInstance(new PrefixedNamingStrategy("G"));
		
		contribute(ModelProvider.class, new ModelProvider() {

			@Override
			public Collection<Class<? extends AbstractEntity>> getModelClasses() {
				Collection<Class<? extends AbstractEntity>> modelClasses = 
						new HashSet<Class<? extends AbstractEntity>>();
				modelClasses.addAll(ClassUtils.findImplementations(AbstractEntity.class, ModelLocator.class));
				return modelClasses;
			}
			
		});
		
		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(UserNameReservation.class, new UserNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});

		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(RepositoryNameReservation.class, new RepositoryNameReservation() {
			
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
		bind(AuthorizationManager.class).to(DefaultAuthorizationManager.class);
		bind(ConfigManager.class).to(DefaultConfigManager.class);
		bind(DataManager.class).to(DefaultDataManager.class);
		bind(CommentManager.class).to(DefaultCommentManager.class);
		bind(CommentReplyManager.class).to(DefaultCommentReplyManager.class);
		bind(PullRequestManager.class).to(DefaultPullRequestManager.class);
		bind(PullRequestUpdateManager.class).to(DefaultPullRequestUpdateManager.class);
		bind(RepositoryManager.class).to(DefaultRepositoryManager.class);
		bind(TeamManager.class).to(DefaultTeamManager.class);
		bind(UserManager.class).to(DefaultUserManager.class);
		bind(VerificationManager.class).to(DefaultVerificationManager.class);
		bind(ReviewInvitationManager.class).to(DefaultReviewInvitationManager.class);
		bind(ReviewManager.class).to(DefaultReviewManager.class);
		bind(MailManager.class).to(DefaultMailManager.class);
		bind(BranchWatchManager.class).to(DefaultBranchWatchManager.class);
		bind(PullRequestWatchManager.class).to(DefaultPullRequestWatchManager.class);
		bind(NotificationManager.class).to(DefaultNotificationManager.class);

		bind(AbstractRealm.class).to(SecurityRealm.class);

		contribute(RepositoryListener.class, DefaultPullRequestManager.class);
		contribute(PullRequestListener.class, DefaultNotificationManager.class);
		contribute(PullRequestListener.class, DefaultPullRequestWatchManager.class);
		contribute(LifecycleListener.class, DefaultPullRequestManager.class);
		contribute(LifecycleListener.class, DefaultUserManager.class);
		contribute(LifecycleListener.class, DefaultRepositoryManager.class);
		
		contribute(ConfigListener.class, new ConfigListener() {

			@Override
			public void onSave(Config config) {
			}
			
		});
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return GitPlex.class;
	}

}
