package io.onedev.server.buildspec.step;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import java.util.List;

public abstract class CompositeStep extends Step {

	private static final long serialVersionUID = 1L;
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, 
								ParamCombination paramCombination) {
		return new CompositeFacade(getActions(build, jobExecutor, jobToken, paramCombination));
	}
	
	protected abstract List<Action> getActions(Build build, JobExecutor jobExecutor, String jobToken,
											   ParamCombination paramCombination);

}
