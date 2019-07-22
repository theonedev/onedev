package io.onedev.server.plugin.htmlreport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.protocol.http.WebApplication;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.model.Build;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.OnePageMapper;
import io.onedev.server.web.mapper.OneResourceMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;

public class HtmlReportModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobOutcome.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(JobHtmlReport.class);
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(JobOutcome.getLockKey(build, JobHtmlReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File startPagesFile = new File(JobOutcome.getOutcomeDir(build, JobHtmlReport.DIR), JobHtmlReport.START_PAGES);
						if (startPagesFile.exists()) { 
							Map<String, String> startPages;
							if (startPagesFile.exists()) 
								startPages = SerializationUtils.deserialize(FileUtils.readFileToByteArray(startPagesFile));
							else
								startPages = new HashMap<>();
							for (String reportName: startPages.keySet()) 
								tabs.add(new HtmlReportTab(reportName));
						}
						return null;
					}
					
				});
				return tabs;
			}
			
			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new OnePageMapper("projects/${project}/builds/${build}/html-reports/${report}", HtmlReportPage.class));
				application.mount(new OneResourceMapper("downloads/projects/${project}/builds/${build}/html-reports/${path}", 
						new HtmlReportDownloadResourceReference()));
			}
			
		});		
		
	}

}
