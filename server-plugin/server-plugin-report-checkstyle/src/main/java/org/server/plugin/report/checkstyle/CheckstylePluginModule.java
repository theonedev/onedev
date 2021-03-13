package org.server.plugin.report.checkstyle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CheckstyleMetric;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.StatisticsMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CheckstylePluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobReport.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(JobESLintReport.class, JobCheckstyleReport.class);
			}
			
		});
		
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, CheckstyleMetric.class).isEmpty()) {
					String query = String.format("%s \"last month\"", 
							BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since));
					PageParameters params = CheckstyleStatsPage.paramsOf(project, query);
					menuItems.add(new SidebarMenuItem.Page(null, "Checkstyle", CheckstyleStatsPage.class, params));
				}
				return menuItems;
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(CodeProblemContribution.class, new CodeProblemContribution() {
			
			@Override
			public List<CodeProblem> getCodeProblems(Build build, String blobPath, String reportName) {
				return LockUtils.read(build.getReportCategoryLockKey(JobCheckstyleReport.DIR), new Callable<List<CodeProblem>>() {

					@SuppressWarnings("unchecked")
					@Override
					public List<CodeProblem> call() throws Exception {
						List<CodeProblem> problems = new ArrayList<>();
						File categoryDir = build.getReportCategoryDir(JobCheckstyleReport.DIR);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()) 
										&& (reportName == null || reportName.equals(reportDir.getName()))) { 
									File violationsFile = new File(reportDir, JobCheckstyleReport.VIOLATION_FILES + "/" + blobPath);
									if (violationsFile.exists()) {
										try (InputStream is = new BufferedInputStream(new FileInputStream(violationsFile))) {
											for (ViolationFile.Violation violation: 
													(List<ViolationFile.Violation>) SerializationUtils.deserialize(is)) {
												problems.add(new CodeProblem(violation.getRange(), violation.getMessage(), 
														violation.getSeverity()));
											}
										}
									}
								}
							}
						}
						return problems;
					}
					
				});
				
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(build.getReportCategoryLockKey(JobCheckstyleReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (build.getReportCategoryDir(JobCheckstyleReport.DIR).exists()) {
							for (File reportDir: build.getReportCategoryDir(JobCheckstyleReport.DIR).listFiles()) {
								if (!reportDir.isHidden() && SecurityUtils.canAccessReport(build, reportDir.getName())) {
									tabs.add(new BuildReportTab(reportDir.getName(), CheckstyleFilesPage.class, 
											CheckstyleRulesPage.class, CheckstyleStatsPage.class));
								}
							}
						}
						return null;
					}
					
				});
				return tabs;
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/checkstyle-reports/${report}/files", 
						CheckstyleFilesPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/checkstyle-reports/${report}/rules", 
						CheckstyleRulesPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/stats/checkstyle", CheckstyleStatsPage.class));
			}
			
		});				
		
	}

}
