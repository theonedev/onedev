package io.onedev.server.markdown;

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;

import io.onedev.server.OneDev;

public class ExternalLinkFormatter<N extends InlineLinkNode> implements NodeFormattingHandler.CustomNodeFormatter<N> {

	@Override
	public void render(InlineLinkNode node, NodeFormatterContext context, MarkdownWriter markdown) {
		markdown.append("[");
		context.renderChildren(node);
		markdown.append("](");
		markdown.append(OneDev.getInstance(MarkdownService.class).toExternal(node.getUrl().toString()));
		markdown.append(")");
	}

}
