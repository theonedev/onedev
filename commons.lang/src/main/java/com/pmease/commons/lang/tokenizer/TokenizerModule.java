package com.pmease.commons.lang.tokenizer;

import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class TokenizerModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(Tokenizers.class).to(DefaultTokenizers.class);
		
		contributeFromPackage(Tokenizer.class, Tokenizer.class);
	}

}
