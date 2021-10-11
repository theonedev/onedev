package io.onedev.server.plugin.report.problem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
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
public class ProblemReportModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, ProblemMetric.class).isEmpty()) {
					String query = String.format("%s \"last month\"", 
							BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since));
					PageParameters params = ProblemStatsPage.paramsOf(project, query);
					menuItems.add(new SidebarMenuItem.Page(null, "Checkstyle", ProblemStatsPage.class, params));
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
				return LockUtils.read(ProblemReport.getReportLockKey(build), new Callable<List<CodeProblem>>() {

					@SuppressWarnings("unchecked")
					@Override
					public List<CodeProblem> call() throws Exception {
						List<CodeProblem> problems = new ArrayList<>();
						File categoryDir = new File(build.getPublishDir(), ProblemReport.CATEGORY);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()) 
										&& (reportName == null || reportName.equals(reportDir.getName()))) { 
									File file = new File(reportDir, ProblemReport.FILES_DIR + "/" + blobPath);
									if (file.exists()) {
										try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
											problems.addAll((List<CodeProblem>) SerializationUtils.deserialize(is));
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
				LockUtils.read(ProblemReport.getReportLockKey(build), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File categoryDir = new File(build.getPublishDir(), ProblemReport.CATEGORY);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (!reportDir.isHidden() && SecurityUtils.canAccessReport(build, reportDir.getName())) {
									tabs.add(new BuildReportTab(reportDir.getName(), ProblemReportPage.class, 
											ProblemStatsPage.class));
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
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/problem-reports/${report}", 
						ProblemReportPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/stats/checkstyle", ProblemStatsPage.class));
			}
			
		});			
	}

}
