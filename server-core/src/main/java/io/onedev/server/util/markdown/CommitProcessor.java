package io.onedev.server.util.markdown;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

public class CommitProcessor implements MarkdownProcessor {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");

	private static final Pattern PATTERN_COMMIT = Pattern.compile("(^|\\s)([a-z0-9]{40})($|\\s)");
	
	protected String toHtml(Project project, ObjectId commitId) {
		CharSequence url = RequestCycle.get().urlFor(
				CommitDetailPage.class, CommitDetailPage.paramsOf(project, commitId.name())); 
		return String.format("<a href='%s' class='commit reference' data-reference='%s'>%s</a>", url, commitId.name(), 
				GitUtils.abbreviateSHA(commitId.name()));
	}

	@Override
	public void process(Document rendered, @Nullable Project project, Object context) {
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				return !HtmlUtils.hasAncestor(node, IGNORED_TAGS);
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(rendered);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN_COMMIT.matcher(node.getWholeText());
			while (matcher.find()) {
				String commitHash = matcher.group(2);
				String commitTag;
				ObjectId commitId = ObjectId.fromString(commitHash);
				if (project.getRevCommit(commitId, false) != null) {
					commitTag = toHtml(project, commitId);
				} else {
					commitTag = commitId.name();
				}
				HtmlUtils.appendReplacement(matcher, node, matcher.group(1) + commitTag + matcher.group(3));
			}
			HtmlUtils.appendTail(matcher, node);
		}
	}
	
}
