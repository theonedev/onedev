package com.pmease.commons.lang;

import com.pmease.commons.lang.tokenizers.DefaultTokenizers;
import com.pmease.commons.lang.tokenizers.Tokenizer;
import com.pmease.commons.lang.tokenizers.Tokenizers;
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
		contributeFromPackage(Tokenizer.class, Tokenizer.class);
		
		bind(Extractors.class).to(DefaultExtractors.class);
		bind(Tokenizers.class).to(DefaultTokenizers.class);
	}

}
