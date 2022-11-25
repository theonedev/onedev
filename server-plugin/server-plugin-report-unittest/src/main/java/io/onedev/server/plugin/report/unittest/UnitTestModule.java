package io.onedev.server.plugin.report.unittest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.UnitTestMetric;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.StatisticsMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class UnitTestModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				return projectManager.runOnProjectServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
						.filter(it->SecurityUtils.canAccessReport(build, it.getTitle()))
						.collect(Collectors.toList());
			}
			
			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, UnitTestMetric.class).isEmpty()) {
					PageParameters params = UnitTestStatsPage.paramsOf(project);
					menuItems.add(new SidebarMenuItem.Page(null, "Unit Test", UnitTestStatsPage.class, params));
				}
				return menuItems;
			}
			
			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new ProjectPageMapper("${project}/~builds/${build}/unit-test/${report}/test-suites", UnitTestSuitesPage.class));
				application.mount(new ProjectPageMapper("${project}/~builds/${build}/unit-test/${report}/test-cases", UnitTestCasesPage.class));
				application.mount(new ProjectPageMapper("${project}/~stats/unit-test", UnitTestStatsPage.class));
			}
			
		});		
		
	}

	private static class GetBuildTabs implements ClusterTask<List<BuildTab>> {

		private static final long serialVersionUID = 1L;
		
		private final Long projectId;
		
		private final Long buildNumber;
		
		public GetBuildTabs(Long projectId, Long buildNumber) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
		}

		@Override
		public List<BuildTab> call() throws Exception {
			return LockUtils.read(UnitTestReport.getReportLockName(projectId, buildNumber), new Callable<List<BuildTab>>() {

				@Override
				public List<BuildTab> call() throws Exception {
					List<BuildTab> tabs = new ArrayList<>();
					File categoryDir = new File(Build.getDir(projectId, buildNumber), UnitTestReport.CATEGORY);
					if (categoryDir.exists()) {
						for (File reportDir: categoryDir.listFiles()) {
							if (!reportDir.isHidden()) {
								tabs.add(new BuildReportTab(reportDir.getName(), UnitTestSuitesPage.class, 
										UnitTestCasesPage.class, UnitTestStatsPage.class));
							}
						}
					}
					Collections.sort(tabs, new Comparator<BuildTab>() {

						@Override
						public int compare(BuildTab o1, BuildTab o2) {
							return o1.getTitle().compareTo(o1.getTitle());
						}
						
					});
					return tabs;
				}
				
			});
		}
		
	}
}