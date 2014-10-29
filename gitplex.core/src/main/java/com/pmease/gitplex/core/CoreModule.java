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
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.extensionpoint.PullRequestListeners;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.impl.DefaultStorageManager;
import com.pmease.gitplex.core.model.ModelLocator;
import com.pmease.gitplex.core.model.PullRequest;
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
		
		contribute(PullRequestListener.class, new PullRequestListener() {

			@Override
			public void onOpened(PullRequest request) {
			}

			@Override
			public void onUpdated(PullRequest request) {
			}

			@Override
			public void onVoted(PullRequest request) {
			}

			@Override
			public void onIntegrated(PullRequest request) {
			}

			@Override
			public void onDiscarded(PullRequest request) {
			}

			@Override
			public void onIntegrationPreviewCalculated(PullRequest request) {
			}

			@Override
			public void onCommented(PullRequest request) {
			}
			
		});
		
		bind(PullRequestListeners.class);
		
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

		bind(StorageManager.class).to(DefaultStorageManager.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return GitPlex.class;
	}

}
