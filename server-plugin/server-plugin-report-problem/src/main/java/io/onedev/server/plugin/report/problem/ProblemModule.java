package io.onedev.server.plugin.report.problem;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
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
import static io.onedev.server.plugin.report.problem.ProblemReport.CATEGORY;
import static io.onedev.server.plugin.report.problem.ProblemReport.getReportLockName;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ProblemModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(StatisticsMenuContribution.class, new StatisticsMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricManager.class).getAccessibleReportNames(project, ProblemMetric.class).isEmpty()) {
					PageParameters params = ProblemStatsPage.paramsOf(project);
					menuItems.add(new SidebarMenuItem.Page(null, "Checkstyle", ProblemStatsPage.class, params));
				}
				return menuItems;
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(CodeProblemContribution.class, (build, blobPath, reportName) -> {
			Long projectId = build.getProject().getId();
			Long buildNumber = build.getNumber();
			
			Map<String, Collection<CodeProblem>> problemsMap = getProjectManager().runOnActiveServer(
					projectId, new GetCodeProblems(projectId, buildNumber, blobPath, reportName));
			
			List<CodeProblem> problems = new ArrayList<>();
			for (var entry: problemsMap.entrySet()) {
				if (SecurityUtils.canAccessReport(build, entry.getKey()))
					problems.addAll(entry.getValue());
			}
			
			return problems;
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
				return 300;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, application -> {
			application.mount(new ProjectPageMapper("${project}/~builds/${build}/problem/${report}", 
					ProblemReportPage.class));
			application.mount(new ProjectPageMapper("${project}/~stats/problem", ProblemStatsPage.class));
		});

		contribute(BuildStorageSyncer.class, ((projectId, buildNumber, activeServer) -> {
			OneDev.getInstance(ProjectManager.class).syncDirectory(projectId, 
					getProjectRelativeStoragePath(buildNumber) + "/" + CATEGORY,
					getReportLockName(projectId, buildNumber), activeServer);
		}));
	}

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private static class GetCodeProblems implements ClusterTask<Map<String, Collection<CodeProblem>>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String blobPath;
		
		private final String reportName;
		
		public GetCodeProblems(Long projectId, Long buildNumber, String blobPath, @Nullable String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.blobPath = blobPath;
			this.reportName = reportName;
		}

		@Override
		public Map<String, Collection<CodeProblem>> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				Map<String, Collection<CodeProblem>> problems = new HashMap<>();
				File categoryDir = new File(Build.getStorageDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir) && (reportName == null || reportName.equals(reportDir.getName()))) { 
							File file = new File(reportDir, ProblemReport.FILES + "/" + blobPath);
							if (file.exists()) {
								try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
									problems.put(reportDir.getName(), (Collection<CodeProblem>) SerializationUtils.deserialize(is));
								}
							}
						}
					}
				}
				return problems;
			});
		}
		
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
				File categoryDir = new File(Build.getStorageDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!reportDir.isHidden() && !isVersionFile(reportDir)) {
							tabs.add(new BuildReportTab(reportDir.getName(), ProblemReportPage.class, 
									ProblemStatsPage.class));
						}
					}
				}
				Collections.sort(tabs, (o1, o2) -> o1.getTitle().compareTo(o1.getTitle()));
				return tabs;
			});
		}
		
	}	
	
}
