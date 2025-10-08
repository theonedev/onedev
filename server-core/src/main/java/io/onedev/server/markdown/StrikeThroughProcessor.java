package io.onedev.server.markdown;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

public class StrikeThroughProcessor implements HtmlProcessor {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("~~(.*?)~~");

	@Override
	public void process(Document document, Project project,
						@Nullable BlobRenderContext blobRenderContext,
						@Nullable SuggestionSupport suggestionSupport,
						boolean forExternal) {
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (HtmlUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return node.getWholeText().contains("~~"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor.traverse(visitor, document);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) 
				HtmlUtils.appendReplacement(matcher, node, "<del>" + matcher.group(1) + "</del>");
			HtmlUtils.appendTail(matcher, node);
		}
	}
	
}
