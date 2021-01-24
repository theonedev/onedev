package io.onedev.server.plugin.report.html;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblem.Severity;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.code.LineCoverage;
import io.onedev.server.code.LineCoverageContribution;
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

		contribute(CodeProblemContribution.class, new CodeProblemContribution() {
			
			@Override
			public List<CodeProblem> getCodeProblems(Build build, String blobPath, String reportName) {
				return Lists.newArrayList(
						new CodeProblem(new PlanarRange(21, 1, 21, 5), "message1", Severity.ERROR),
						new CodeProblem(new PlanarRange(227, 1, 230, 10), "message4", Severity.WARNING)
					);
			}
			
		});
		
		contribute(LineCoverageContribution.class, new LineCoverageContribution() {
			
			@Override
			public List<LineCoverage> getLineCoverages(Build build, String blobPath, String reportName) {
				return Lists.newArrayList(new LineCoverage(21, 21, 1), new LineCoverage(226, 229, 0));
			}
			
		});
	}

}
