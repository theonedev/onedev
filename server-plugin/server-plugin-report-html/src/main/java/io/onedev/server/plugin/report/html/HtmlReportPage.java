package io.onedev.server.plugin.report.html;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static io.onedev.server.plugin.report.html.PublishHtmlReportStep.START_PAGE;
import static java.nio.charset.StandardCharsets.*;

public class HtmlReportPage extends BuildReportPage {

	private static final String PARAM_REPORT = "report";
	
	private final String reportName;
	
	public HtmlReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Long projectId = getProject().getId();
		Long buildNumber = getBuild().getNumber();

		var startPage = OneDev.getInstance(ProjectManager.class).runOnActiveServer(
				projectId, 
				new ReadPublishedFile(projectId, buildNumber, getReportName(), START_PAGE));
		
		PageParameters params = HtmlReportDownloadResource.paramsOf(
				getBuild(), reportName, new String(startPage, UTF_8));
		CharSequence startPageUrl = RequestCycle.get()
				.urlFor(new HtmlReportDownloadResourceReference(), params);
		
		add(new WebMarkupContainer("report") {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(CssHeaderItem.forReference(new HtmlReportCssResourceReference()));
			}

		}.add(AttributeAppender.append("src", startPageUrl.toString())).add(AttributeAppender.append("class", "d-flex flex-grow-1")));
	}

	@Override
	public String getReportName() {
		return reportName;
	}

}
