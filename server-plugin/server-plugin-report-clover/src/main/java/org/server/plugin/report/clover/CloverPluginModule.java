package org.server.plugin.report.clover;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.onedev.server.code.LineCoverageContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CloverMetric;
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
public class CloverPluginModule extends AbstractPluginModule {

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
				return Sets.newHashSet(JobJestCoverageReport.class, JobCloverReport.class);
			}
			
		});
		
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, CloverMetric.class).isEmpty()) {
					String query = String.format("%s \"last month\"", 
							BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since));
					PageParameters params = CloverStatsPage.paramsOf(project, query);
					menuItems.add(new SidebarMenuItem.Page(null, "Clover Coverage", CloverStatsPage.class, params));
				}
				return menuItems;
			}
			
			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(LineCoverageContribution.class, new LineCoverageContribution() {
			
			@Override
			public Map<Integer, Integer> getLineCoverages(Build build, String blobPath, String reportName) {
				return LockUtils.read(build.getReportCategoryLockKey(JobCloverReport.DIR), new Callable<Map<Integer, Integer>>() {

					@Override
					public Map<Integer, Integer> call() throws Exception {
						Map<Integer, Integer> coverages = new HashMap<>();
						File categoryDir = build.getReportCategoryDir(JobCloverReport.DIR);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()) 
										&& (reportName == null || reportName.equals(reportDir.getName()))) { 
									File testCountFile = new File(reportDir, JobCloverReport.TEST_COUNTS_DIR + "/" + blobPath);
									if (testCountFile.exists()) {
										try (InputStream is = new BufferedInputStream(new FileInputStream(testCountFile))) {
											@SuppressWarnings("unchecked")
											Map<Integer, Integer> deserialized = (Map<Integer, Integer>) SerializationUtils.deserialize(is);
											deserialized.forEach((key, value) -> {
												coverages.merge(key, value, (v1, v2) -> v1+v2);
											});
										}
									}
								}
							}
						}
						return coverages;
					}
					
				});
				
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(build.getReportCategoryLockKey(JobCloverReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (build.getReportCategoryDir(JobCloverReport.DIR).exists()) {
							for (File reportDir: build.getReportCategoryDir(JobCloverReport.DIR).listFiles()) {
								if (!reportDir.isHidden() && SecurityUtils.canAccessReport(build, reportDir.getName())) { 
									tabs.add(new BuildReportTab(reportDir.getName(), CloverReportPage.class, 
											CloverStatsPage.class));
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
				return 200;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/clover-reports/${report}", CloverReportPage.class));
				application.mount(new DynamicPathPageMapper("projects/${project}/stats/clover", CloverStatsPage.class));
			}
			
		});				
		
	}
	
}
