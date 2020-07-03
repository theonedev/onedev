package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;

public class IssueProcessor extends ReferenceParser implements MarkdownProcessor {
	
	public IssueProcessor() {
		super(Issue.class);
	}

	@Override
	public void process(Document document, @Nullable Project project, Object context) {
		parseReferences(document, project);
	}

	@Override
	protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
		CharSequence url = RequestCycle.get().urlFor(
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(referenceable)); 
		return String.format("<a href='%s' class='issue reference' data-reference='%s'>%s</a>", 
				url, referenceable.toString(), referenceText);
	}
	
}
