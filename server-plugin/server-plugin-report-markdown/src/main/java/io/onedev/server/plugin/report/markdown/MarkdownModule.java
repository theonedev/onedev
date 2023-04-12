package io.onedev.server.plugin.report.markdown;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.replica.BuildStorageSyncer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryContribution;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryPart;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getProjectRelativeStoragePath;
import static io.onedev.server.model.Build.getStorageDir;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MarkdownModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return PublishReportStep.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(PublishMarkdownReportStep.class, PublishPullRequestMarkdownReportStep.class);
			}
			
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
		
		contribute(PullRequestSummaryContribution.class, new PullRequestSummaryContribution() {

			@Override
			public List<PullRequestSummaryPart> getParts(PullRequest request) {
				List<PullRequestSummaryPart> parts = new ArrayList<>();
				Long projectId = request.getProject().getId();
				for (Build build: request.getCurrentBuilds()) {
					Long buildNumber = build.getNumber();
					for (PullRequestSummaryPart part: getProjectManager().runOnActiveServer(projectId, new GetPullRequestSummaryParts(projectId, buildNumber))) {
						if (SecurityUtils.canAccessReport(build, part.getReportName()))
							parts.add(part);
					}
				}
				return parts;
			}

			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, application -> application.mount(new ProjectPageMapper(
				"${project}/~builds/${build}/markdown/${report}", 
				MarkdownReportPage.class)));

		contribute(BuildStorageSyncer.class, ((projectId, buildNumber, activeServer) -> {
			getProjectManager().syncDirectory(projectId, 
					getProjectRelativeStoragePath(buildNumber) + "/" + PublishMarkdownReportStep.CATEGORY,
					PublishMarkdownReportStep.getReportLockName(projectId, buildNumber), activeServer);
			getProjectManager().syncDirectory(projectId, 
					getProjectRelativeStoragePath(buildNumber) + "/" + PublishPullRequestMarkdownReportStep.CATEGORY,
					PublishPullRequestMarkdownReportStep.getReportLockName(projectId, buildNumber), activeServer);
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
			return read(PublishMarkdownReportStep.getReportLockName(projectId, buildNumber), () -> {
				List<BuildTab> tabs = new ArrayList<>();
				File categoryDir = new File(getStorageDir(projectId, buildNumber), PublishMarkdownReportStep.CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir))
							tabs.add(new MarkdownReportTab(reportDir.getName()));
					}
				}
				Collections.sort(tabs, (o1, o2) -> o1.getTitle().compareTo(o1.getTitle()));
				return tabs;
			});
		}
		
	}
	
	private static class GetPullRequestSummaryParts implements ClusterTask<List<PullRequestSummaryPart>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;
		
		private final Long buildNumber;
		
		private GetPullRequestSummaryParts(Long projectId, Long buildNumber) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
		}
		
		@Override
		public List<PullRequestSummaryPart> call() {
			return read(PublishPullRequestMarkdownReportStep.getReportLockName(projectId, buildNumber), () -> {
				List<PullRequestSummaryPart> parts = new ArrayList<>();
				File categoryDir = new File(getStorageDir(projectId, buildNumber), PublishPullRequestMarkdownReportStep.CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir))
							parts.add(new PullRequestSummaryMarkdownPart(projectId, buildNumber, reportDir.getName()));
					}
				}
				return parts;
			});
		}
		
	}
}
