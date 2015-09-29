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
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TaskListNode;
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
	public String parse(final String markdown) {
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
		PegDownPlugins pegDownPlugins = builder.build();
		PegDownProcessor processor = new PegDownProcessor(ALL_WITH_OPTIONALS, pegDownPlugins);

		RootNode ast = processor.parseMarkdown(markdown.toCharArray());
		
		List<ToHtmlSerializerPlugin> serializerPlugins = new ArrayList<>();
		for (MarkdownExtension extension: extensions) {
			if (extension.getHtmlSerializers() != null) {
				for (ToHtmlSerializerPlugin each: extension.getHtmlSerializers())
					serializerPlugins.add(each);
			}
		}

		return new ToHtmlSerializer(new LinkRenderer(), serializerPlugins) {

			@Override
			public void visit(ListItemNode node) {
				
		        if (node instanceof TaskListNode) {
		            // vsch: #185 handle GitHub style task list items, these are a bit messy because the <input> checkbox needs to be
		            // included inside the optional <p></p> first grand-child of the list item, first child is always RootNode
		            // because the list item text is recursively parsed.
		            Node firstChild = node.getChildren().get(0).getChildren().get(0);
		            boolean firstIsPara = firstChild instanceof ParaNode;
		            int indent = node.getChildren().size() > 1 ? 2 : 0;
		            boolean startWasNewLine = printer.endsWithNewLine();

		            int startIndex = node.getStartIndex();
		            
		            // PegDown has problems displaying "* [ ]hello", so we check this 
		            // case and render it as a normal list item
		            boolean validTaskList = markdown.substring(0, startIndex).trim().endsWith("]");
		            if (validTaskList)
			            printer.println().print("<li class=\"task-list-item\">").indent(indent);
		            else 
			            printer.println().print("<li>").indent(indent);
		            
		            if (firstIsPara) {
		                printer.println().print("<p>");
		                if (validTaskList)
		                	printer.print("<input data-mdstart=" + startIndex + " type='checkbox' class='task-list-item-checkbox'" + (((TaskListNode) node).isDone() ? " checked='checked'" : "") + "></input>");
		                visitChildren((SuperNode) firstChild);

		                // render the other children, the p tag is taken care of here
		                visitChildrenSkipFirst(node);
		                printer.print("</p>");
		            } else {
		            	if (validTaskList)
		            		printer.print("<input data-mdstart=" + node.getStartIndex() + " type='checkbox' class='task-list-item-checkbox'" + (((TaskListNode) node).isDone() ? " checked='checked'" : "") + "></input>");
		                visitChildren(node);
		            }
		            printer.indent(-indent).printchkln(indent != 0).print("</li>")
		                    .printchkln(startWasNewLine);
		        } else {
		            printConditionallyIndentedTag(node, "li");
		        }
			}
			
		}.toHtml(ast);	
	}

	@Override
	public String parseAndProcess(String markdown) {
		return process(parse(markdown));
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
