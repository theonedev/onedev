package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.annotation.Editable;

import java.util.List;

@Editable(order=500, name="Dependency job finished")
public class DependencyFinishedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof BuildFinished) {
			BuildFinished buildFinished = (BuildFinished) event;
			Build build = buildFinished.getBuild();
			for (JobDependency dependency: job.getJobDependencies()) {
				if (dependency.getJobName().equals(build.getJobName()) 
						&& (!dependency.isRequireSuccessful() || build.getStatus() == Status.SUCCESSFUL)) {
					for (ParamSupply param: dependency.getJobParams()) {
						if (!param.isSecret()) {
							List<String> paramValue = build.getParamMap().get(param.getName());
							if (!param.getValuesProvider().getValues(null, null).contains(paramValue))
								return null;
						}
					}
					
					return new TriggerMatch(build.getRefName(), build.getRequest(), build.getIssue(), getParams(),
							"Dependency job '" + dependency.getJobName() + "' is finished");
				}
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return "When dependency jobs finished";
	}

}
