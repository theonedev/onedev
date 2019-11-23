package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;

public class BuildProcessor extends BuildParser implements MarkdownProcessor {
	
	@Override
	public void process(@Nullable Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected Build findReferenceable(Project project, long number) {
		Build build = super.findReferenceable(project, number);
		if (build != null && SecurityUtils.canAccess(build))
			return build;
		else
			return null;
	}

	@Override
	protected String toHtml(Build build, String text) {
		CharSequence url = RequestCycle.get().urlFor(
				BuildDashboardPage.class, BuildDashboardPage.paramsOf(build, null)); 
		return String.format("<a href='%s' class='build reference' data-reference=%d>%s</a>", url, build.getId(), text);
	}
	
}
