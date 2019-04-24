package io.onedev.server.ci.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public abstract class JobTrigger implements Validatable, Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobParam> params = new ArrayList<>();

	@Editable(name="Trigger Parameters", order=1000, description="Specify parameters to trigger the job")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}
	
	public abstract boolean matches(ProjectEvent event, Job job);
	
	public abstract String getDescription();

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> paramNames = new HashSet<>();
		boolean isValid = true;
		for (JobParam param: params) {
			if (BuildConstants.ALL_FIELDS.contains(param.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Reserved param name: " + param.getName())
						.addPropertyNode("params").addConstraintViolation();
			} else if (paramNames.contains(param.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate param name: " + param.getName())
						.addPropertyNode("params").addConstraintViolation();
			} else {
				paramNames.add(param.getName());
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
}
