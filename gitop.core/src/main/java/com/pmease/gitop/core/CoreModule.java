package com.pmease.gitop.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.hibernate.cfg.NamingStrategy;

import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.PrefixedNamingStrategy;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.FilterChainConfigurator;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitop.core.model.ModelLocator;
import com.pmease.gitop.core.permission.UserRealm;
import com.pmease.gitop.core.validation.ProjectNameReservation;
import com.pmease.gitop.core.validation.UserNameReservation;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(AbstractRealm.class).to(UserRealm.class);
		
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
		
		contribute(ServletConfigurator.class, CoreServletConfigurator.class);
		
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
		contribute(ProjectNameReservation.class, new ProjectNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});

		contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

			@Override
			public void configure(FilterChainManager filterChainManager) {
				filterChainManager.createChain("/**/info/refs", "noSessionCreation, authcBasic");
				filterChainManager.createChain("/**/git-upload-pack", "noSessionCreation, authcBasic");
				filterChainManager.createChain("/**/git-receive-pack", "noSessionCreation, authcBasic");
			}
			
		});
		
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Gitop.class;
	}

}
