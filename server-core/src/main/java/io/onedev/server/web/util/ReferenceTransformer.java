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
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.markdown.BuildParser;
import io.onedev.server.util.markdown.IssueParser;
import io.onedev.server.util.markdown.PullRequestParser;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class ReferenceTransformer implements Function<String, String> {

	private final Project project;
	
	private final String url;
	
	public ReferenceTransformer(Project project, @Nullable String url) {
		this.project = project;
		this.url = url;
	}
	
	@Override
	public String apply(String t) {
		String escaped = "<pre>" + HtmlEscape.escapeHtml5(t) + "</pre>";
		Document doc = Jsoup.parseBodyFragment(escaped);
		
		new IssueParser() {

			@Override
			protected String toHtml(Issue referenceable, String referenceText) {
				return "<a class='embedded-reference' href='" + RequestCycle.get().urlFor(IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(referenceable, null)) + "'>" + referenceText + "</a>";
			}

			@Override
			protected Issue findReferenceable(Project project, long number) {
				if (SecurityUtils.canAccess(project))
					return super.findReferenceable(project, number);
				else
					return null;
			}
			
		}.parseReferences(project, doc);

		new PullRequestParser() {

			@Override
			protected String toHtml(PullRequest referenceable, String referenceText) {
				return "<a class='embedded-reference' href='" + RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(referenceable, null)) + "'>" + referenceText + "</a>";
			}

			@Override
			protected PullRequest findReferenceable(Project project, long number) {
				if (SecurityUtils.canReadCode(project))
					return super.findReferenceable(project, number);
				else
					return null;
			}
			
		}.parseReferences(project, doc);

		new BuildParser() {

			@Override
			protected String toHtml(Build referenceable, String referenceText) {
				return "<a class='embedded-reference' href='" + RequestCycle.get().urlFor(BuildDashboardPage.class, 
						BuildDashboardPage.paramsOf(referenceable, null)) + "'>" + referenceText + "</a>";
			}

			@Override
			protected Build findReferenceable(Project project, long number) {
				Build build= super.findReferenceable(project, number);
				if (build != null && SecurityUtils.canAccess(build))
					return build;
				else
					return null;
			}
			
		}.parseReferences(project, doc);
		
		if (url != null) {
			StringBuilder builder = new StringBuilder();
			for (Node node: Jsoup.parseBodyFragment(doc.body().html()).body().child(0).childNodes()) {
				if (node instanceof TextNode) {
					TextNode textNode = (TextNode) node;
					builder.append("<a href='" + url + "'>" + textNode.getWholeText() + "</a>");
				} else {
					builder.append(node.outerHtml());
				}
			}
			return builder.toString();
		} else {
			return doc.body().child(0).html();
		}
	}
	
}
