package io.onedev.server.plugin.report.coverage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.codequality.LineCoverageContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.model.Project;
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
public class CoverageReportModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, CoverageMetric.class).isEmpty()) {
					PageParameters params = CoverageStatsPage.paramsOf(project);
					menuItems.add(new SidebarMenuItem.Page(null, "Coverage", CoverageStatsPage.class, params));
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
			public Map<Integer, CoverageStatus> getLineCoverages(Build build, String blobPath, String reportName) {
				return LockUtils.read(CoverageReport.getReportLockKey(build), new Callable<Map<Integer, CoverageStatus>>() {

					@Override
					public Map<Integer, CoverageStatus> call() throws Exception {
						Map<Integer, CoverageStatus> coverages = new HashMap<>();
						File categoryDir = new File(build.getPublishDir(), CoverageReport.CATEGORY);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()) 
										&& (reportName == null || reportName.equals(reportDir.getName()))) { 
									File lineCoveragesFile = new File(reportDir, CoverageReport.FILES_DIR + "/" + blobPath);
									if (lineCoveragesFile.exists()) {
										try (InputStream is = new BufferedInputStream(new FileInputStream(lineCoveragesFile))) {
											@SuppressWarnings("unchecked")
											Map<Integer, CoverageStatus> deserialized = (Map<Integer, CoverageStatus>) SerializationUtils.deserialize(is);
											deserialized.forEach((key, value) -> {
												coverages.merge(key, value, (v1, v2) -> v1.mergeWith(v2));
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
				LockUtils.read(CoverageReport.getReportLockKey(build), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File categoryDir = new File(build.getPublishDir(), CoverageReport.CATEGORY);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (!reportDir.isHidden() && SecurityUtils.canAccessReport(build, reportDir.getName())) { 
									tabs.add(new BuildReportTab(reportDir.getName(), CoverageReportPage.class, 
											CoverageStatsPage.class));
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
				application.mount(new BasePageMapper("projects/${project}/builds/${build}/coverage/${report}", CoverageReportPage.class));
				application.mount(new BasePageMapper("projects/${project}/stats/coverage", CoverageStatsPage.class));
			}
			
		});		
		
	}

}
