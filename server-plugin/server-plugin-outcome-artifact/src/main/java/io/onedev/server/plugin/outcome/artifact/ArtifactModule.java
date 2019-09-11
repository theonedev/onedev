package io.onedev.server.plugin.outcome.artifact;

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
import io.onedev.server.ci.job.DependencyPopulator;
import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.model.Build;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.OnePageMapper;
import io.onedev.server.web.mapper.OneResourceMapper;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.BuildTabContribution;

public class ArtifactModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobOutcome.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(JobArtifacts.class);
			}
			
		});
		
		contribute(DependencyPopulator.class, ArtifactsPopulator.class);
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				List<BuildTab> tabs = new ArrayList<>();
				File artifactsDir = JobOutcome.getOutcomeDir(build, JobArtifacts.DIR);
				LockUtils.read(JobOutcome.getLockKey(build, JobArtifacts.DIR), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (artifactsDir.exists()) 
							tabs.add(new BuildTab("Artifacts", BuildArtifactsPage.class));
						return null;
					}
					
				});
				return tabs;
			}
			
			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new OnePageMapper("projects/${project}/builds/${build}/artifacts", BuildArtifactsPage.class));
				application.mount(new OneResourceMapper("downloads/projects/${project}/builds/${build}/artifacts/${path}", 
						new ArtifactDownloadResourceReference()));
			}
			
		});
		
	}

}
