package com.gitplex.server.util.markdown;

import java.util.Collection;

import javax.annotation.Nullable;

import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface MarkdownExtension {
	
	@Nullable
	Collection<Class<? extends Parser>> getInlineParsers();

	@Nullable
	Collection<Class<? extends Parser>> getBlockParsers();
	
	@Nullable
	Collection<ToHtmlSerializerPlugin> getHtmlSerializers();
	
	@Nullable
	Collection<HtmlTransformer> getHtmlTransformers();
}
