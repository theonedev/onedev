package com.pmease.commons.tapestry;

import org.apache.tapestry5.TapestryFilter;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class TapestryModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		bind(TapestryFilter.class).to(CustomTapestryFilter.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return TapestryPlugin.class;
	}

}
