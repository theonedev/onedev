package io.onedev.server.plugin.report.problem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
import io.onedev.server.model.Project;
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
		
		contribute(CodeProblemContribution.class, new CodeProblemContribution() {
			
			@Override
			public List<CodeProblem> getCodeProblems(Build build, String blobPath, String reportName) {
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				Map<String, List<CodeProblem>> problemsMap = getProjectManager().runOnProjectServer(
						projectId, new GetCodeProblems(projectId, buildNumber, blobPath, reportName));
				
				List<CodeProblem> problems = new ArrayList<>();
				for (var entry: problemsMap.entrySet()) {
					if (SecurityUtils.canAccessReport(build, entry.getKey()))
						problems.addAll(entry.getValue());
				}
				
				return problems;
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				return getProjectManager().runOnProjectServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
						.filter(it->SecurityUtils.canAccessReport(build, it.getTitle()))
						.collect(Collectors.toList());
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new ProjectPageMapper("${project}/~builds/${build}/problem/${report}", 
						ProblemReportPage.class));
				application.mount(new ProjectPageMapper("${project}/~stats/problem", ProblemStatsPage.class));
			}
			
		});			
	}

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private static class GetCodeProblems implements ClusterTask<Map<String, List<CodeProblem>>> {

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
		public Map<String, List<CodeProblem>> call() throws Exception {
			return LockUtils.read(ProblemReport.getReportLockName(projectId, buildNumber), new Callable<Map<String, List<CodeProblem>>>() {

				@SuppressWarnings("unchecked")
				@Override
				public Map<String, List<CodeProblem>> call() throws Exception {
					Map<String, List<CodeProblem>> problems = new HashMap<>();
					File categoryDir = new File(Build.getDir(projectId, buildNumber), ProblemReport.CATEGORY);
					if (categoryDir.exists()) {
						for (File reportDir: categoryDir.listFiles()) {
							if (reportName == null || reportName.equals(reportDir.getName())) { 
								File file = new File(reportDir, ProblemReport.FILES_DIR + "/" + blobPath);
								if (file.exists()) {
									try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
										problems.put(reportDir.getName(), (List<CodeProblem>) SerializationUtils.deserialize(is));
									}
								}
							}
						}
					}
					return problems;
				}
				
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
		public List<BuildTab> call() throws Exception {
			return LockUtils.read(ProblemReport.getReportLockName(projectId, buildNumber), new Callable<List<BuildTab>>() {

				@Override
				public List<BuildTab> call() throws Exception {
					List<BuildTab> tabs = new ArrayList<>();
					File categoryDir = new File(Build.getDir(projectId, buildNumber), ProblemReport.CATEGORY);
					if (categoryDir.exists()) {
						for (File reportDir: categoryDir.listFiles()) {
							if (!reportDir.isHidden()) {
								tabs.add(new BuildReportTab(reportDir.getName(), ProblemReportPage.class, 
										ProblemStatsPage.class));
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
