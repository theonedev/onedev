package io.onedev.server.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

@Editable
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ExecuteCondition condition = ExecuteCondition.ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL;
	
	public abstract StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination);
	
	private String name;

	@Editable(order=10)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=10000, description="Under which condition this step should run")
	@NotNull
	public ExecuteCondition getCondition() {
		return condition;
	}

	public void setCondition(ExecuteCondition condition) {
		this.condition = condition;
	}

	public Action getAction(String name, Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return new Action(name, getFacade(build, jobExecutor, jobToken, paramCombination), condition);
	}
	
	public Action getAction(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return getAction(name, build, jobExecutor, jobToken, paramCombination);
	}
	
}
