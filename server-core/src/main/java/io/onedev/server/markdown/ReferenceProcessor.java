package io.onedev.server.markdown;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entityreference.ReferenceParser;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.web.UrlManager;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;

public class ReferenceProcessor implements MarkdownProcessor {

	@Override
	public void process(Document document, @Nullable Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, 
			boolean forExternal) {
		new ReferenceParser(Build.class) {
			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				String url;
				if (RequestCycle.get() != null)
					url = RequestCycle.get().urlFor(BuildDashboardPage.class, BuildDashboardPage.paramsOf(referenceable)).toString();
				else
					url = OneDev.getInstance(UrlManager.class).urlForBuild(referenceable);
				var build = OneDev.getInstance(BuildManager.class).find(referenceable);
				if (build != null && build.getVersion() != null)
					referenceText += " (" + HtmlEscape.escapeHtml5(build.getVersion()) + ")";
				return String.format("<a href='%s' class='build reference' data-reference='%s'>%s</a>",
						url, referenceable.toString(), referenceText);
			}
		}.parseReferences(document, project);
		
		new ReferenceParser(PullRequest.class) {
			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				String url;
				if (RequestCycle.get() != null) 
					url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(referenceable)).toString();
				else 
					url = OneDev.getInstance(UrlManager.class).urlForPullRequest(referenceable);
				return String.format("<a href='%s' class='pull-request reference' data-reference='%s'>%s</a>",
						url, referenceable.toString(), referenceText);
			}
		}.parseReferences(document, project);
		
		new ReferenceParser(Issue.class) {
			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				String url;
				if (RequestCycle.get() != null) 
					url = RequestCycle.get().urlFor(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(referenceable)).toString();
				else 
					url = OneDev.getInstance(UrlManager.class).urlForIssue(referenceable);
				return String.format("<a href='%s' class='issue reference' data-reference='%s'>%s</a>",
						url, referenceable.toString(), referenceText);						
			}
		}.parseReferences(document, project);
	}

}
