package io.onedev.server.buildspec.job.action;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.ActionCondition;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class PostBuildAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;

	@Editable(order=100, description="Specify the condition current build must satisfy to execute this action")
	@ActionCondition
	@NotEmpty
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public abstract void execute(Build build);
	
	public abstract String getDescription();
	
	public void validateWithContext(BuildSpec buildSpec, Job job) {
		io.onedev.server.buildspec.job.action.condition.ActionCondition.parse(job, condition);
	}

}
