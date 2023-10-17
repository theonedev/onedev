package io.onedev.server.markdown;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.validation.validator.ProjectPathValidator;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.TextNodeVisitor;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

public class CommitProcessor implements MarkdownProcessor {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");

	private static final Pattern PATTERN_COMMIT = Pattern.compile(
			"(^|\\W+)((" + ProjectPathValidator.PATTERN.pattern() + "):)?([a-z0-9]{40})($|[^a-z0-9])");
	
	@Override
	public void process(Document document, @Nullable Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, 
			boolean forExternal) {
		if (RequestCycle.get() != null) {
			TextNodeVisitor visitor = new TextNodeVisitor() {
				
				@Override
				protected boolean isApplicable(TextNode node) {
					return !HtmlUtils.hasAncestor(node, IGNORED_TAGS);
				}
			};
			
			NodeTraversor.traverse(visitor, document);
			
			ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
			for (TextNode node : visitor.getMatchedNodes()) {
				Matcher matcher = PATTERN_COMMIT.matcher(node.getWholeText());
				while (matcher.find()) {
					String commitReplacement;
					String commitProjectPath = matcher.group(3);
					String commitHash = matcher.group(5);
					Project commitProject = project;
					String commitPrefix = "";
					if (commitProjectPath != null) {
						commitProject = projectManager.findByPath(commitProjectPath);
						commitPrefix = commitProjectPath + ":";
					}
					if (commitProject != null) {
						ObjectId commitId = ObjectId.fromString(commitHash);
						if (commitProject.getRevCommit(commitId, false) != null) {
							CharSequence url = RequestCycle.get().urlFor(
									CommitDetailPage.class, CommitDetailPage.paramsOf(commitProject, commitId.name()));
							commitReplacement = String.format("<a href='%s' class='commit reference' data-reference='%s'>%s</a>", 
									url, commitPrefix + commitId.name(), commitPrefix + GitUtils.abbreviateSHA(commitId.name()));
						} else {
							commitReplacement = commitPrefix + commitHash;
						}
					} else {
						commitReplacement = commitPrefix + commitHash; 
					}
					HtmlUtils.appendReplacement(matcher, node, matcher.group(1) + commitReplacement + matcher.group(6));
				}
				HtmlUtils.appendTail(matcher, node);
			}
		}
	}
	
}
