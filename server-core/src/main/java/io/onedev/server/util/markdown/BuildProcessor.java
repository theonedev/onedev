package io.onedev.server.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;

public class BuildProcessor extends BuildParser implements MarkdownProcessor {
	
	@Override
	public void process(Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected String toHtml(Build build, String text) {
		CharSequence url = RequestCycle.get().urlFor(
				BuildDashboardPage.class, BuildDashboardPage.paramsOf(build, null)); 
		return String.format("<a href='%s' class='build reference' data-reference=%d>%s</a>", url, build.getId(), text);
	}
	
}
