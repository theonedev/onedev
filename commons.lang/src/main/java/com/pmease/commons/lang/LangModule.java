package com.pmease.commons.lang;

import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class LangModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contributeFromPackage(Extractor.class, Extractor.class);
		
		bind(Extractors.class).to(DefaultExtractors.class);
	}

}
