package io.onedev.server.markdown;

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;

import io.onedev.server.OneDev;
import io.onedev.server.service.UrlService;

public class ExternalImageFormatter<N extends InlineLinkNode> implements NodeFormattingHandler.CustomNodeFormatter<N> {

	@Override
	public void render(InlineLinkNode node, NodeFormatterContext context, MarkdownWriter markdown) {
		markdown.append("![");
		context.renderChildren(node);
		markdown.append("](");
		markdown.append(OneDev.getInstance(UrlService.class).toExternalUrl(node.getUrl().toString()));
		markdown.append(")");
	}

}
