package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryContribution;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryPart;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MarkdownReportModule extends AbstractPluginModule {

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
				return Sets.newHashSet(JobMarkdownReport.class, JobPullRequestMarkdownReport.class);
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				LockUtils.read(build.getReportCategoryLockKey(JobMarkdownReport.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File categoryDir = build.getReportCategoryDir(JobMarkdownReport.DIR);
						if (categoryDir.exists()) {
							for (File reportDir: categoryDir.listFiles()) {
								if (SecurityUtils.canAccessReport(build, reportDir.getName()))
									tabs.add(new MarkdownReportTab(reportDir.getName()));
							}
						}
						return null;
					}
					
				});
				return tabs;
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
				for (Build build: request.getCurrentBuilds()) {
					parts.addAll(LockUtils.read(build.getReportCategoryLockKey(JobMarkdownReport.DIR), new Callable<List<PullRequestSummaryPart>>() {

						@Override
						public List<PullRequestSummaryPart> call() throws Exception {
							List<PullRequestSummaryPart> parts = new ArrayList<>();
							File categoryDir = build.getReportCategoryDir(JobPullRequestMarkdownReport.DIR);
							if (categoryDir.exists()) {
								for (File reportDir: categoryDir.listFiles()) {
									if (SecurityUtils.canAccessReport(build, reportDir.getName()))
										parts.add(new PullRequestSummaryMarkdownPart(reportDir));
								}
							}
							return parts;
						}
						
					}));
				}
				return parts;
			}

			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new DynamicPathPageMapper("projects/${project}/builds/${build}/markdown-reports/${report}/#{path}", MarkdownReportPage.class));
			}
			
		});	
		
	}

}
