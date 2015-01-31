package com.pmease.commons.markdown.extensionpoint;

import java.util.Collection;

import javax.annotation.Nullable;

import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.pmease.commons.loader.ExtensionPoint;

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
