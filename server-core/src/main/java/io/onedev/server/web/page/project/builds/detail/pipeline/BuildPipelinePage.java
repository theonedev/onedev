package io.onedev.server.web.page.project.builds.detail.pipeline;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.job.jobinfo.JobInfoButton;
import io.onedev.server.web.component.pipeline.PipelinePanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import java.util.List;

public class BuildPipelinePage extends BuildDetailPage {

	public BuildPipelinePage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessPipeline(getBuild());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PipelinePanel("pipeline") {

			@Override
			protected List<Job> getJobs() {
				return getBuild().getProject().getBuildSpec(getBuild().getCommitId()).getJobs();
			}

			@Override
			protected int getActiveJobIndex() {
				for (int i=0; i<getJobs().size(); i++) {
					if (getBuild().getJobName().equals(getJobs().get(i).getName()))
						return i;
				}
				return -1;
			}

			@Override
			protected Component renderJob(String componentId, int jobIndex) {
				return new JobInfoButton(componentId) {

					@Override
					protected Project getProject() {
						return BuildPipelinePage.this.getProject();
					}

					@Override
					protected ObjectId getCommitId() {
						return getBuild().getCommitId();
					}

					@Override
					protected String getJobName() {
						return getJobs().get(jobIndex).getName();
					}

					@Override
					protected Build getActiveBuild() {
						return getBuild();
					}

				};
			}
			
		});
	}
}
