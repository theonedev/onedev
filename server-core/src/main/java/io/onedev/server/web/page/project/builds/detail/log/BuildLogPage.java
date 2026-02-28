package io.onedev.server.web.page.project.builds.detail.log;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import io.onedev.server.job.JobService;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.logging.LogPanel;
import io.onedev.server.web.component.logging.PauseSupport;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.resource.BuildLogResource;
import io.onedev.server.web.resource.BuildLogResourceReference;

public class BuildLogPage extends BuildDetailPage {

	@Inject
	private JobService jobService;

	public BuildLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new LogPanel("log") {

			@Override
			protected LoggingSupport getLoggingSupport() {
				return getBuild().getLoggingSupport();
			}

			@Nullable
			@Override
			protected PauseSupport getPauseSupport() {
				return new PauseSupport() {

					@Override
					public boolean isPaused() {
						return getBuild().isPaused();
					}

					@Override
					public boolean canResume() {
						return SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName());
					}

					@Override
					public void resume() {
						jobService.resume(getBuild());						
					}

					@Override
					public String getStatusChangeObservable() {
						return Build.getDetailChangeObservable(getBuild().getId());
					}
					
				};
			}

		});
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessLog(getBuild());
	}

	public Component renderOptions(String componentId) {
		Fragment fragment = new Fragment(componentId, "optionsFrag", this);
		fragment.add(new ResourceLink<Void>("download", new BuildLogResourceReference(), 
				BuildLogResource.paramsOf(projectModel.getObject().getId(), getBuild().getNumber())));
		return fragment;
	}
}
