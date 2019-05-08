package io.onedev.server.plugin.htmlreport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.util.QueryPosition;

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

		String startPage = LockUtils.read(JobOutcome.getLockKey(getBuild(), JobHtmlReport.DIR), new Callable<String>() {

			@Override
			public String call() throws Exception {
				Map<String, String> startPages;
				File infoFile = new File(JobOutcome.getOutcomeDir(getBuild(), JobHtmlReport.DIR), JobHtmlReport.START_PAGES);
				if (infoFile.exists()) 
					startPages = SerializationUtils.deserialize(FileUtils.readFileToByteArray(infoFile));
				else
					startPages = new HashMap<>();
				return startPages.get(reportName);
			}
			
		});
		
		if (startPage != null) {
			PageParameters params = HtmlReportDownloadResource.paramsOf(getProject(), getBuild().getNumber(), startPage);
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
			throw new OneException("Unable to find html report: " + reportName);
		}
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	public String getReportName() {
		return reportName;
	}

	public static PageParameters paramsOf(Build build, @Nullable QueryPosition position, String reportName) {
		PageParameters params = paramsOf(build, position);
		params.add(PARAM_REPORT, reportName);
		return params;
	}
}
