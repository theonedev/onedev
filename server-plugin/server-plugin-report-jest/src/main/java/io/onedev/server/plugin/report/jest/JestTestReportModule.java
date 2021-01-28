package io.onedev.server.plugin.report.jest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.JestTestMetric;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.StatisticsMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class JestTestReportModule extends AbstractPluginModule {

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
				return Sets.newHashSet(JobJestReport.class);
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(build.getReportLockKey(JobJestReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (build.getReportDir(JobJestReport.DIR).exists()) {
							for (File reportDir: build.getReportDir(JobJestReport.DIR).listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName())) 
									tabs.add(new JestReportTab(reportDir.getName()));
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
		
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, JestTestMetric.class).isEmpty()) {
					String query = String.format("%s \"last month\"", 
							BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since));
					PageParameters params = JestTestStatsPage.paramsOf(project, query);
					menuItems.add(new SidebarMenuItem.Page(null, "Jest", 
							JestTestStatsPage.class, params));
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
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/jest-reports/${report}/test-suites", JestTestSuitesPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/jest-reports/${report}/test-cases", JestTestCasesPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/stats/jest", JestTestStatsPage.class));
			}
			
		});		
	}

	private static class JestReportTab extends BuildTab {

		private static final long serialVersionUID = 1L;

		public JestReportTab(String title) {
			super(title, JestTestSuitesPage.class, JestTestReportPage.class);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				private static final long serialVersionUID = 1L;

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = JestTestReportPage.paramsOf(
							page.getBuild(), getTitle());
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
				}
				
			};
		}

		@Override
		public boolean isActive(Page currentPage) {
			if (super.isActive(currentPage)) {
				JestTestReportPage jestReportPage = (JestTestReportPage) currentPage;
				return getTitle().equals(jestReportPage.getReportName());
			} else {
				return false;
			}
		}
		
	}
}
