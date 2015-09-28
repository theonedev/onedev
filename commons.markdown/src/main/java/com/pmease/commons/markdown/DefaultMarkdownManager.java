package com.pmease.commons.markdown;

import static org.pegdown.Extensions.ALL_WITH_OPTIONALS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.pegdown.LinkRenderer;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.commons.markdown.extensionpoint.MarkdownExtension;
import com.pmease.commons.util.JsoupUtils;

@Singleton
public class DefaultMarkdownManager implements MarkdownManager {

	private final Set<MarkdownExtension> extensions;

	@Inject
	public DefaultMarkdownManager(Set<MarkdownExtension> extensions) {
		this.extensions = extensions;
	}

	@Override
	public String parse(String markdown) {
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
		PegDownProcessor processor = new PegDownProcessor(ALL_WITH_OPTIONALS, plugins);

		RootNode ast = processor.parseMarkdown(markdown.toCharArray());
		
		List<ToHtmlSerializerPlugin> serializers = new ArrayList<>();
		for (MarkdownExtension extension: extensions) {
			if (extension.getHtmlSerializers() != null) {
				for (ToHtmlSerializerPlugin each: extension.getHtmlSerializers())
					serializers.add(each);
			}
		}

		return new ToHtmlSerializer(new LinkRenderer(), serializers).toHtml(ast);	
	}

	@Override
	public String parseAndProcess(String markdown) {
		String rawHtml = parse(markdown);
		return process(rawHtml);
	}
	
	@Override
	public String process(String rawHtml) {
		String html = JsoupUtils.sanitize(rawHtml);

		List<HtmlTransformer> transformers = new ArrayList<>();
		for (MarkdownExtension extension: extensions) {
			if (extension.getHtmlTransformers() != null) {
				for (HtmlTransformer transformer: extension.getHtmlTransformers())
					transformers.add(transformer);
			}
		}
		
		if (!transformers.isEmpty()) {
			Element body = Jsoup.parseBodyFragment(html).body();
			for (HtmlTransformer transformer: transformers)
				body = transformer.transform(body);
			return body.html();
		} else {
			return html;
		}
		
	}

	@Override
	public String escape(String markdown) {
		markdown = StringEscapeUtils.escapeHtml4(markdown);
		markdown = StringUtils.replace(markdown, "\n", "<br>");
		return markdown;
	}

}
