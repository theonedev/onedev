package com.pmease.gitplex.core.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.commons.util.TextNodeVisitor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;

public class PullRequestParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)#(\\d+)(?=($|\\s+))");

	public Collection<PullRequest> parseRequests(String html) {
		return parseRequests(Jsoup.parseBodyFragment(html).body());		
	}
	
	public Collection<PullRequest> parseRequests(Element body) {
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
		tranversor.traverse(body);
		
		Dao dao = GitPlex.getInstance(Dao.class);
		
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
