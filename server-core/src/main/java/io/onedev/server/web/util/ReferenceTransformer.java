package io.onedev.server.web.util;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.markdown.ReferenceParser;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class ReferenceTransformer implements Function<String, String> {

	private final Project project;
	
	private final String url;
	
	public ReferenceTransformer(Project project) {
		this(project, null);
	}
	
	public ReferenceTransformer(Project project, @Nullable String url) {
		this.project = project;
		this.url = url;
	}
	
	@Override
	public String apply(String t) {
		String escaped = "<pre>" + HtmlEscape.escapeHtml5(t) + "</pre>";
		Document doc = Jsoup.parseBodyFragment(escaped);
		
		new ReferenceParser(Issue.class) {

			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(referenceable)) + "'>" + referenceText + "</a>";
			}

		}.parseReferences(doc, project);

		new ReferenceParser(PullRequest.class) {

			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(referenceable)) + "'>" + referenceText + "</a>";
			}

		}.parseReferences(doc, project);

		new ReferenceParser(Build.class) {

			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(BuildDashboardPage.class, 
						BuildDashboardPage.paramsOf(referenceable)) + "'>" + referenceText + "</a>";
			}

		}.parseReferences(doc, project);
		
		if (url != null) {
			StringBuilder builder = new StringBuilder();
			for (Node node: Jsoup.parseBodyFragment(doc.body().html()).body().child(0).childNodes()) {
				if (node instanceof TextNode) 
					builder.append(String.format("<a href='%s'>%s</a>", url, node.outerHtml()));
				else 
					builder.append(node.outerHtml());
			}
			return builder.toString();
		} else {
			return doc.body().child(0).html();
		}
	}
	
}
