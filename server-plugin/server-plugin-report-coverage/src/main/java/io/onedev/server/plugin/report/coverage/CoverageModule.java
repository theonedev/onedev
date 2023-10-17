package io.onedev.server.plugin.report.coverage;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.codequality.LineCoverageContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.model.Project;
import io.onedev.server.replica.BuildStorageSyncer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.StatisticsMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;
import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getProjectRelativeStoragePath;
import static io.onedev.server.model.Build.getStorageDir;
import static io.onedev.server.plugin.report.coverage.CoverageReport.CATEGORY;
import static io.onedev.server.plugin.report.coverage.CoverageReport.getReportLockName;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoverageModule extends AbstractPluginModule {

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
		
		contribute(LineCoverageContribution.class, (build, blobPath, reportName) -> {
			Long projectId = build.getProject().getId();
			Long buildNumber = build.getNumber();
			
			Map<Integer, CoverageStatus> coverages = new HashMap<>();
			for (var entry: getProjectManager().runOnActiveServer(projectId, new GetLineCoverages(projectId, buildNumber, blobPath, reportName)).entrySet()) {
				if (SecurityUtils.canAccessReport(build, entry.getKey())) {
					entry.getValue().forEach((key, value) -> {
						coverages.merge(key, value, CoverageStatus::mergeWith);
					});
				}
			}
			
			return coverages;
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				return getProjectManager().runOnActiveServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
						.filter(it->SecurityUtils.canAccessReport(build, it.getTitle()))
						.collect(Collectors.toList());
			}
			
			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, application -> {
			application.mount(new ProjectPageMapper("${project}/~builds/${build}/coverage/${report}", CoverageReportPage.class));
			application.mount(new ProjectPageMapper("${project}/~stats/coverage", CoverageStatsPage.class));
		});

		contribute(BuildStorageSyncer.class, ((projectId, buildNumber, activeServer) -> {
			getProjectManager().syncDirectory(projectId, 
					getProjectRelativeStoragePath(buildNumber) + "/" + CATEGORY, 
					getReportLockName(projectId, buildNumber), activeServer);
		}));
		
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
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
		public List<BuildTab> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				List<BuildTab> tabs = new ArrayList<>();
				File categoryDir = new File(getStorageDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!reportDir.isHidden() && !isVersionFile(reportDir)) {
							tabs.add(new BuildReportTab(reportDir.getName(), CoverageReportPage.class, 
									CoverageStatsPage.class));
						}
					}
				}
				Collections.sort(tabs, (o1, o2) -> o1.getTitle().compareTo(o1.getTitle()));
				return tabs;
			});
		}
		
	}	
	
	private static class GetLineCoverages implements ClusterTask<Map<String, Map<Integer, CoverageStatus>>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String blobPath;
		
		private final String reportName;
		
		public GetLineCoverages(Long projectId, Long buildNumber, String blobPath, @Nullable String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.blobPath = blobPath;
			this.reportName = reportName;
		}
		
		@Override
		public Map<String, Map<Integer, CoverageStatus>> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				Map<String, Map<Integer, CoverageStatus>> coverages = new HashMap<>();
				File categoryDir = new File(getStorageDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (reportName == null || reportName.equals(reportDir.getName())) { 
							File lineCoveragesFile = new File(reportDir, CoverageReport.FILES + "/" + blobPath);
							if (lineCoveragesFile.exists()) {
								try (InputStream is = new BufferedInputStream(new FileInputStream(lineCoveragesFile))) {
									coverages.put(reportDir.getName(), (Map<Integer, CoverageStatus>) SerializationUtils.deserialize(is));
								}
							}
						}
					}
				}
				return coverages;
			});
			
		}
		
	}
}
