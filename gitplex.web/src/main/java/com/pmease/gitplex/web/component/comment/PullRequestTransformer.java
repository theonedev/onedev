package com.pmease.gitplex.web.component.comment;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.commons.util.TextNodeVisitor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;

public class PullRequestTransformer implements HtmlTransformer {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)#(\\d+)(?=($|\\s+))");
	
	@Override
	public Element transform(Element body) {
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
				if (request != null)
					requestTag = toHtml(request);
				else 
					requestTag = "#" + requestId;
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + requestTag + matcher.group(3));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return body;
	}

	private String toHtml(PullRequest request) {
		CharSequence url = RequestCycle.get().urlFor(
				RequestOverviewPage.class, RequestOverviewPage.paramsOf(request)); 
		return String.format("<a href='%s' class='request'>#%d</a>", url, request.getId());
	}

}
