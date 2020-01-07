package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;

public class BuildProcessor extends ReferenceParser implements MarkdownProcessor {
	
	public BuildProcessor() {
		super(Build.class);
	}

	@Override
	public void process(@Nullable Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
		CharSequence url = RequestCycle.get().urlFor(
				BuildDashboardPage.class, BuildDashboardPage.paramsOf(referenceable, null)); 
		return String.format("<a href='%s' class='build reference' data-reference='%s'>%s</a>", 
				url, referenceable.toString(), referenceText);
	}
	
}
