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
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.utils.StringUtils;

public class IssueParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)(issue\\s+)#(\\d+)(?=($|\\s+))", Pattern.CASE_INSENSITIVE);

	public Collection<Issue> parseIssues(Project project, String rendered) {
		return parseIssues(project, Jsoup.parseBodyFragment(rendered));		
	}
	
	public Collection<Issue> parseIssues(Project project, Document document) {
		Collection<Issue> references = new HashSet<>();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (JsoupUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return StringUtils.deleteWhitespace(node.getWholeText()).trim().contains("issue#"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(document);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) {
				Long issueNumber = Long.valueOf(matcher.group(3));
				String issueTag;
				Issue issue = OneDev.getInstance(IssueManager.class).find(project, issueNumber);
				if (issue != null) {
					references.add(issue);
					issueTag = toHtml(issue);
				} else {
					issueTag = "#" + issueNumber;
				}
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + matcher.group(2) + issueTag + matcher.group(4));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return references;
	}

	protected String toHtml(Issue issue) {
		return "#" + issue.getNumber();
	}
	
}
