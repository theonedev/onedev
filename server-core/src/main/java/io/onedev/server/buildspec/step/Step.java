package io.onedev.server.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	private ExecuteCondition condition = ExecuteCondition.ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL;
	
	public abstract Executable getExecutable(Build build, ParamCombination paramCombination);

	@Editable(order=10000, description="Under which condition this step should run")
	@NotNull
	public ExecuteCondition getCondition() {
		return condition;
	}

	public void setCondition(ExecuteCondition condition) {
		this.condition = condition;
	}

	public Action getAction(Build build, ParamCombination paramCombination) {
		return new Action(getExecutable(build, paramCombination), condition);
	}
	
}
