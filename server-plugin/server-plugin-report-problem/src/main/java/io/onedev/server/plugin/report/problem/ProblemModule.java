package io.onedev.server.plugin.report.problem;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getProjectRelativeDirPath;
import static io.onedev.server.plugin.report.problem.ProblemReport.CATEGORY;
import static io.onedev.server.plugin.report.problem.ProblemReport.getReportLockName;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;
import static io.onedev.server.web.translation.Translation._T;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Build;
import io.onedev.server.model.ProblemMetric;
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
public class ProblemModule extends AbstractPluginModule {

	private static final Logger logger = LoggerFactory.getLogger(ProblemModule.class);
	
	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectMenuContribution.class, new ProjectMenuContribution() {
			
			@Override
			public List<SidebarMenuItem> getMenuItems(Project project) {
				List<SidebarMenuItem> menuItems = new ArrayList<>();
				if (!OneDev.getInstance(BuildMetricService.class).getAccessibleReportNames(project, ProblemMetric.class).isEmpty()) {
					PageParameters params = ProblemStatsPage.paramsOf(project);
					menuItems.add(new SidebarMenuItem.Page(null, "Checkstyle", ProblemStatsPage.class, params));
				}
				return Lists.newArrayList(new SidebarMenuItem.SubMenu("stats", _T("Statistics"), menuItems));
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(CodeProblemContribution.class, (build, blobPath, reportName) -> {
			Long projectId = build.getProject().getId();
			Long buildNumber = build.getNumber();
			
			Map<String, Collection<CodeProblem>> problemsMap = getProjectService().runOnActiveServer(
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
				
				return getProjectService().runOnActiveServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
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
			OneDev.getInstance(ProjectService.class).syncDirectory(projectId,
					getProjectRelativeDirPath(buildNumber) + "/" + CATEGORY,
					getReportLockName(projectId, buildNumber), activeServer);
		}));
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
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

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Collection<CodeProblem>> call() {
			return read(getReportLockName(projectId, buildNumber), () -> {
				Map<String, Collection<CodeProblem>> problems = new HashMap<>();
				File categoryDir = new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir) && (reportName == null || reportName.equals(reportDir.getName()))) { 
							File file = new File(reportDir, ProblemReport.FILES + "/" + blobPath);
							if (file.exists()) {
								try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
									problems.put(reportDir.getName(), (Collection<CodeProblem>) SerializationUtils.deserialize(is));
								} catch (SerializationException e) {
									logger.error("Error reading problem report: " + file, e);
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
				File categoryDir = new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), CATEGORY);
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
