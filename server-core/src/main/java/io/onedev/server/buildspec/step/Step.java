package io.onedev.server.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ExecuteCondition condition = ExecuteCondition.ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL;
	
	public abstract StepFacade getFacade(Build build, String jobToken, ParamCombination paramCombination);
	
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

	public Action getAction(String name, Build build, String jobToken, ParamCombination paramCombination) {
		return new Action(name, getFacade(build, jobToken, paramCombination), condition);
	}
	
	public Action getAction(Build build, String jobToken, ParamCombination paramCombination) {
		return getAction(name, build, jobToken, paramCombination);
	}
	
	public static String getGroupedType(Class<? extends Step> stepClass) {
		Editable editable = stepClass.getAnnotation(Editable.class);
		if (editable != null && editable.group().length() != 0)
			return editable.group() + " / " + EditableUtils.getDisplayName(stepClass);
		else
			return EditableUtils.getDisplayName(stepClass);
	}
	
}
