package io.onedev.server.entityreference;

import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class LinkTransformer implements BiFunction<EntityReference, String, String> {
	
	private final String noneReferenceUrl;
	
	public LinkTransformer(@Nullable String noneReferenceUrl) {
		this.noneReferenceUrl = noneReferenceUrl;
	}
	
	@Override
	public String apply(@Nullable EntityReference reference, String text) {
		text = HtmlEscape.escapeHtml5(text);
		if (reference instanceof IssueReference) {
			return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(IssueActivitiesPage.class,
					IssueActivitiesPage.paramsOf(reference.getProject(), reference.getNumber())) + "'>" + text + "</a>";
		} else if (reference instanceof BuildReference) {
			return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(BuildDashboardPage.class,
					BuildDashboardPage.paramsOf(reference.getProject(), reference.getNumber())) + "'>" + text + "</a>";
		} else if (reference instanceof PullRequestReference) {
			return "<a class='embedded-reference link-info' href='" + RequestCycle.get().urlFor(PullRequestActivitiesPage.class,
					PullRequestActivitiesPage.paramsOf(reference.getProject(), reference.getNumber())) + "'>" + text + "</a>";
		} else if (noneReferenceUrl != null) {
			return String.format("<a href='%s'>%s</a>", noneReferenceUrl, text);			
		} else {
			return text;
		}
	}
	
}
