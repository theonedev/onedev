package com.pmease.gitplex.core.manager.impl;

import static org.pegdown.Extensions.ALL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.pegdown.LinkRenderer;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.pmease.commons.util.JsoupUtils;
import com.pmease.gitplex.core.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.extensionpoint.MarkdownExtension;
import com.pmease.gitplex.core.manager.MarkdownManager;

@Singleton
public class DefaultMarkdownManager implements MarkdownManager {

	private final Set<MarkdownExtension> extensions;

	@Inject
	public DefaultMarkdownManager(Set<MarkdownExtension> extensions) {
		this.extensions = extensions;
	}

	@Override
	public RootNode toAST(String markdown) {
		PegDownPlugins.Builder builder = new PegDownPlugins.Builder();
		for (MarkdownExtension extension: extensions) {
			if (extension.getInlineParsers() != null) {
				for (Class<? extends Parser> each: extension.getInlineParsers())
					builder.withPlugin(each);
			}
			if (extension.getBlockParsers() != null) {
				for (Class<? extends Parser> each: extension.getBlockParsers())
					builder.withPlugin(each);
			}
		}
		PegDownPlugins plugins = builder.build();
		PegDownProcessor processor = new PegDownProcessor(ALL, plugins);
		return processor.parseMarkdown(markdown.toCharArray());
	}

	@Override
	public String toHtml(RootNode ast, boolean sanitizeHtml, boolean applyTransformers) {
		List<ToHtmlSerializerPlugin> serializers = new ArrayList<>();
		for (MarkdownExtension extension: extensions) {
			if (extension.getHtmlSerializers() != null) {
				for (ToHtmlSerializerPlugin each: extension.getHtmlSerializers())
					serializers.add(each);
			}
		}

		String html = new ToHtmlSerializer(new LinkRenderer(), serializers).toHtml(ast);	
		
		if (sanitizeHtml)
			html = JsoupUtils.sanitize(html);

		List<HtmlTransformer> transformers = new ArrayList<>();
		if (applyTransformers) {
			for (MarkdownExtension extension: extensions) {
				if (extension.getHtmlTransformers() != null) {
					for (HtmlTransformer transformer: extension.getHtmlTransformers())
						transformers.add(transformer);
				}
			}
		}
		
		if (!transformers.isEmpty()) {
			Element body = Jsoup.parseBodyFragment(html).body();
			for (HtmlTransformer transformer: transformers) {
				body = transformer.transform(body);
			}
			return body.html();
		} else {
			return html;
		}
		
	}

	@Override
	public String toHtml(String markdown, boolean sanitizeHtml, boolean applyTransformers) {
		return toHtml(toAST(markdown), sanitizeHtml, applyTransformers);
	}
	
}
