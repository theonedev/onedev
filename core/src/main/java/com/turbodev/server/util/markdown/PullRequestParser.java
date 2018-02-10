package com.turbodev.server.util.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.turbodev.server.TurboDev;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.util.JsoupUtils;
import com.turbodev.server.util.TextNodeVisitor;

public class PullRequestParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)#(\\d+)(?=($|\\s+))");

	public Collection<PullRequest> parseRequests(String rendered) {
		return parseRequests(Jsoup.parseBodyFragment(rendered));		
	}
	
	public Collection<PullRequest> parseRequests(Document document) {
		Collection<PullRequest> references = new HashSet<>();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (JsoupUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return node.getWholeText().contains("#"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(document);
		
		Dao dao = TurboDev.getInstance(Dao.class);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) {
				Long requestId = Long.valueOf(matcher.group(2));
				String requestTag;
				PullRequest request = dao.get(PullRequest.class, requestId);
				if (request != null) {
					references.add(request);
					requestTag = toHtml(request);
				} else {
					requestTag = "#" + requestId;
				}
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + requestTag + matcher.group(3));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return references;
	}

	protected String toHtml(PullRequest request) {
		return "#" + request.getId();
	}
	
}
