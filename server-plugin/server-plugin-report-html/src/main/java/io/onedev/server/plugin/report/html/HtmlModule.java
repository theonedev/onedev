package io.onedev.server.plugin.report.html;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.replica.BuildStorageSyncer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BaseResourceMapper;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportTab;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.server.model.Build.getProjectRelativeStoragePath;
import static io.onedev.server.plugin.report.html.PublishHtmlReportStep.CATEGORY;
import static io.onedev.server.plugin.report.html.PublishHtmlReportStep.getReportLockName;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class HtmlModule extends AbstractPluginModule {

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
				return Sets.newHashSet(PublishHtmlReportStep.class);
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
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, application -> {
			application.mount(new ProjectPageMapper("${project}/~builds/${build}/html/${report}", HtmlReportPage.class));
			application.mount(new BaseResourceMapper("~downloads/projects/${project}/builds/${build}/html/${report}",
					new HtmlReportDownloadResourceReference()));
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
				File categoryDir = new File(Build.getStorageDir(projectId, buildNumber), CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!reportDir.isHidden() && !isVersionFile(reportDir)) {
							tabs.add(new BuildReportTab(reportDir.getName(), HtmlReportPage.class, null));
						}
					}
				}
				Collections.sort(tabs, (o1, o2) -> o1.getTitle().compareTo(o1.getTitle()));
				return tabs;
			});
		}
		
	}
}