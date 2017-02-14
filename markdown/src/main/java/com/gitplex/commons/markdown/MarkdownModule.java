package com.gitplex.commons.markdown;

import java.util.Collection;

import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.gitplex.commons.markdown.extensionpoint.HtmlTransformer;
import com.gitplex.commons.markdown.extensionpoint.MarkdownExtension;
import com.google.common.collect.Lists;
import com.gitplex.calla.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MarkdownModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(MarkdownExtension.class, new MarkdownExtension() {
			
			@Override
			public Collection<Class<? extends Parser>> getInlineParsers() {
				return null;
			}
			
			@Override
			public Collection<ToHtmlSerializerPlugin> getHtmlSerializers() {
				return null;
			}
			
			@Override
			public Collection<Class<? extends Parser>> getBlockParsers() {
				return null;
			}

			@Override
			public Collection<HtmlTransformer> getHtmlTransformers() {
				return Lists.newArrayList((HtmlTransformer)new NormalizeTransformer());
			}
			
		});
		bind(MarkdownManager.class).to(DefaultMarkdownManager.class);
	}

}
