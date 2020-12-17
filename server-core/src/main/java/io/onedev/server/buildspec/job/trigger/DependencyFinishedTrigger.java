package io.onedev.server.buildspec.job.trigger;

import java.util.List;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name="Dependency job finished")
public class DependencyFinishedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public SubmitReason matchesWithoutProject(ProjectEvent event, Job job) {
		if (event instanceof BuildFinished) {
			BuildFinished buildFinished = (BuildFinished) event;
			Build build = buildFinished.getBuild();
			for (JobDependency dependency: job.getJobDependencies()) {
				if (dependency.getJobName().equals(build.getJobName()) 
						&& (!dependency.isRequireSuccessful() || build.getStatus() == Status.SUCCESSFUL)) {
					for (ParamSupply param: dependency.getJobParams()) {
						if (!param.isSecret()) {
							List<String> paramValue = build.getParamMap().get(param.getName());
							if (!param.getValuesProvider().getValues().contains(paramValue))
								return null;
						}
					}
					return new SubmitReason() {

						@Override
						public String getRefName() {
							return build.getRefName();
						}

						@Override
						public PullRequest getPullRequest() {
							return build.getRequest();
						}

						@Override
						public String getDescription() {
							return "Dependency job '" + dependency.getJobName() + "' is finished";
						}
						
					};
				}
			}
		}
		return null;
	}

	@Override
	public String getDescriptionWithoutProject() {
		return "When dependency jobs finished";
	}

}
