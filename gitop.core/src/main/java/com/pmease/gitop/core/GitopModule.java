package com.pmease.gitop.core;

import java.util.Collection;
import java.util.HashSet;

import org.hibernate.cfg.NamingStrategy;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.PrefixedNamingStrategy;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitop.core.model.ModelLocator;
import com.pmease.gitop.core.permission.UserRealm;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitopModule extends AbstractPluginModule {

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
		
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Gitop.class;
	}

}
