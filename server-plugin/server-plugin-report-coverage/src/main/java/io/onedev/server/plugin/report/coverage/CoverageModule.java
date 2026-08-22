package io.onedev.server.plugin.report.coverage;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getProjectRelativeDirPath;
import static io.onedev.server.codequality.CoverageStats.CATEGORY;
import static io.onedev.server.codequality.CoverageStats.getReportLockName;
import static io.onedev.server.util.SiteSyncUtils.isVersionFile;
import static io.onedev.server.web.translation.Translation._T;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Build;
import io.onedev.server.model.CoverageMetric;
import io.onedev.server.model.Project;
import io.onedev.server.replica.BuildStorageSyncer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.ProjectMenuContribution;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoverageModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(ProjectMenuContribution.class, new ProjectMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricService.class).getAccessibleReportNames(project, CoverageMetric.class).isEmpty()) {
					PageParameters params = CoverageStatsPage.paramsOf(project);
					menuItems.add(new SidebarMenuItem.Page(null, "Coverage", CoverageStatsPage.class, params));
				}
				return Lists.newArrayList(new SidebarMenuItem.SubMenu("stats", _T("Statistics"), menuItems));
			}
			
			@Override
			public int getOrder() {
				return 200;
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				return getProjectService().runOnActiveServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
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
			getProjectService().syncDirectory(projectId, 
					getProjectRelativeDirPath(buildNumber) + "/" + CATEGORY, 
					getReportLockName(projectId, buildNumber), activeServer);
		}));
		
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
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
				File categoryDir = new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), CATEGORY);
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
	
}
