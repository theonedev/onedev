package io.onedev.server.plugin.report.html;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.mapper.DynamicPathResourceMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;

public class HtmlReportModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobReport.class;
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
				LockUtils.read(build.getReportLockKey(JobHtmlReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (build.getReportDir(JobHtmlReport.DIR).exists()) {
							for (File reportDir: build.getReportDir(JobHtmlReport.DIR).listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()))
									tabs.add(new HtmlReportTab(reportDir.getName()));
							}
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
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/html-reports/${report}", HtmlReportPage.class));
				application.mount(new DynamicPathResourceMapper("downloads/projects/${project}/builds/${build}/html-reports/${report}/${path}", 
						new HtmlReportDownloadResourceReference()));
			}
			
		});		

		/*
		contribute(CodeProblemContribution.class, new CodeProblemContribution() {
			
			@Override
			public List<CodeProblem> getCodeProblems(Build build, String blobPath, String reportName) {
				String longMessage = "This is a very long message and we have a very \n"
						+ "good practice oover This <b>is a very</b> long message and \n"
						+ "we have a very good practice oover This is a very long message and we have a very good practice oover";
				return Lists.newArrayList(
						new CodeProblem(new PlanarRange(0, 1, 1, 5), longMessage, Severity.ERROR),
						new CodeProblem(new PlanarRange(8, 6, 9, 10), reportName, Severity.WARNING),
						new CodeProblem(new PlanarRange(14, 1, 14, 5), "This is an error message", Severity.ERROR),
						new CodeProblem(new PlanarRange(19, 6, 19, 10), longMessage + "\n" + longMessage, Severity.WARNING)
					);
			}
			
		});
		
		contribute(LineCoverageContribution.class, new LineCoverageContribution() {
			
			@Override
			public List<LineCoverage> getLineCoverages(Build build, String blobPath, String reportName) {
				if (reportName == null)
					return Lists.newArrayList(new LineCoverage(5, 8, 1), new LineCoverage(12, 15, 0));
				else
					return Lists.newArrayList(new LineCoverage(5, 8, 1));
			}
			
		});
		*/
	}

}
