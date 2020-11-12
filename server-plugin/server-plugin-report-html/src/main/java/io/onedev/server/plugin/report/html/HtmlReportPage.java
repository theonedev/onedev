package io.onedev.server.plugin.report.html;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

@SuppressWarnings("serial")
public class HtmlReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";
	
	private final String reportName;
	
	public HtmlReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String startPage = LockUtils.read(getBuild().getReportLockKey(JobHtmlReport.DIR), new Callable<String>() {

			@Override
			public String call() throws Exception {
				File startPageFile = new File(getBuild().getReportDir(JobHtmlReport.DIR), 
						reportName + "/" + JobHtmlReport.START_PAGE);
				if (startPageFile.exists())
					return FileUtils.readFileToString(startPageFile, StandardCharsets.UTF_8);
				else
					return null;
			}
			
		});
		
		if (startPage != null) {
			PageParameters params = HtmlReportDownloadResource.paramsOf(getProject(), getBuild().getNumber(), 
					reportName, startPage);
			CharSequence startPageUrl = RequestCycle.get().urlFor(new HtmlReportDownloadResourceReference(), params);
			add(new WebMarkupContainer("htmlReport") {
				
				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
					response.render(JavaScriptHeaderItem.forReference(new HtmlReportResourceReference()));
					String script = String.format("onedev.server.htmlReport.onWindowLoad('%s');", getMarkupId(true));
					response.render(OnLoadHeaderItem.forScript(script));
				}

			}.add(AttributeAppender.append("src", startPageUrl.toString())));
		} else {
			add(new Label("htmlReport", "No html report published") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("div");
				}
				
			}.add(AttributeAppender.append("class", "alert alert-custom alert-notice alert-light-warning mb-0")));
		}
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessReport(getBuild(), reportName);
	}
	
	public String getReportName() {
		return reportName;
	}

	public static PageParameters paramsOf(Build build, String reportName) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_REPORT, reportName);
		return params;
	}
}
