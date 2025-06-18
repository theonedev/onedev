package io.onedev.server.buildspec.step;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;

import io.onedev.k8shelper.PruneBuilderCacheFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.KubernetesAware;

@Editable(order=260, name="Prune Builder Cache", group = DOCKER_IMAGE, description="" +
		"Prune image cache of docker buildx builder. This step calls docker builder prune command " +
		"to remove cache of buildx builder specified in server docker executor or remote docker executor")
public class PruneBuilderCacheStep extends Step {

	private static final long serialVersionUID = 1L;
	
	private String options;

	@Editable(order=100, description = "Optionally specify options for docker builder prune command")
	@ReservedOptions({"-f", "--force", "--builder"})
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return new PruneBuilderCacheFacade(getOptions());
	}
	
	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return executor instanceof DockerAware && !(executor instanceof KubernetesAware);
	}

}
