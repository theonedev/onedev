package com.pmease.gitplex.search;

import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.gitplex.core.listeners.RefListener;
import com.pmease.gitplex.core.listeners.RepositoryListener;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class SearchModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(IndexManager.class).to(DefaultIndexManager.class);
		bind(SearchManager.class).to(DefaultSearchManager.class);
		
		contribute(IndexListener.class, DefaultSearchManager.class);
		contribute(RepositoryListener.class, DefaultIndexManager.class);
		contribute(RefListener.class, DefaultIndexManager.class);
	}

}
