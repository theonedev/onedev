package io.onedev.server.plugin.report.unittest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.UnitTestMetric;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BasePageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.StatisticsMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class UnitTestReportModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(UnitTestReport.getReportLockKey(build), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File categoryDir = new File(build.getPublishDir(), UnitTestReport.CATEGORY);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (!reportDir.isHidden() && SecurityUtils.canAccessReport(build, reportDir.getName())) {
									tabs.add(new BuildReportTab(reportDir.getName(), UnitTestSuitesPage.class, 
											UnitTestCasesPage.class, UnitTestStatsPage.class));
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
		
		// put your guice bindings here
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new BasePageMapper("projects/${project}/builds/${build}/unit-test/${report}/test-suites", UnitTestSuitesPage.class));
				application.mount(new BasePageMapper("projects/${project}/builds/${build}/unit-test/${report}/test-cases", UnitTestCasesPage.class));
				application.mount(new BasePageMapper("projects/${project}/stats/unit-test", UnitTestStatsPage.class));
			}
			
		});		
		
	}

}