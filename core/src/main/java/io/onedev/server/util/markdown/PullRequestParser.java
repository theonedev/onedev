package io.onedev.server.util.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.utils.StringUtils;

public class PullRequestParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)(pull\\s*request\\s+)#(\\d+)(?=($|\\s+))", Pattern.CASE_INSENSITIVE);

	public Collection<PullRequest> parseRequests(Project project, String rendered) {
		return parseRequests(project, Jsoup.parseBodyFragment(rendered));		
	}
	
	public Collection<PullRequest> parseRequests(Project project, Document document) {
		Collection<PullRequest> references = new HashSet<>();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (JsoupUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return StringUtils.deleteWhitespace(node.getWholeText()).trim().contains("pullrequest#"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(document);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) {
				Long requestNumber = Long.valueOf(matcher.group(3));
				String requestTag;
				PullRequest request = OneDev.getInstance(PullRequestManager.class).find(project, requestNumber);
				if (request != null) {
					references.add(request);
					requestTag = toHtml(request);
				} else {
					requestTag = "#" + requestNumber;
				}
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + matcher.group(2) + requestTag + matcher.group(4));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return references;
	}

	protected String toHtml(PullRequest request) {
		return "#" + request.getNumber();
	}
	
}
