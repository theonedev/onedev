package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;

public class IssueProcessor extends IssueParser implements MarkdownProcessor {
	
	@Override
	public void process(@Nullable Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected Issue findReferenceable(Project project, long number) {
		if (SecurityUtils.canAccess(project))
			return super.findReferenceable(project, number);
		else
			return null;
	}

	@Override
	protected String toHtml(Issue issue, String text) {
		CharSequence url = RequestCycle.get().urlFor(
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null)); 
		return String.format("<a href='%s' class='issue reference' data-reference=%d>%s</a>", url, issue.getId(), text);
	}
	
}
