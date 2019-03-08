package io.onedev.server.ci.jobtrigger;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Dependency;
import io.onedev.server.ci.Job;
import io.onedev.server.ci.jobparam.JobParam;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build2.BuildFinished;
import io.onedev.server.model.Build2;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name="When dependency jobs finished running")
public class DependencyBuiltTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof BuildFinished) {
			BuildFinished buildFinished = (BuildFinished) event;
			Build2 build = buildFinished.getBuild();
			for (Dependency dependency: job.getDependencies()) {
				if (dependency.getJob().equals(build.getJob())) {
					for (JobParam param: dependency.getParams()) {
						String paramValue = build.getParamMap().get(param.getName());
						if (!param.getValueProvider().getValues().contains(paramValue))
							return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "When dependency job finished running";
	}

}
