package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.dependency.DependencyEditPanel;

@Editable
@ClassValidating
public class Dependency implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private List<JobParam> params = new ArrayList<>();
	
	@Editable(order=100)
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(name="Job Parameters", order=200, description="Specify parameters of the dependency job. Dependency is satisfied only when "
			+ "there are builds of the job with same set of parameters specified here")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		DependencyEditPanel editor = OneContext.get().getComponent().findParent(DependencyEditPanel.class);
		List<String> choices = new ArrayList<>();
		Job belongingJob = editor.getBelongingJob();
		for (Job job: editor.getEditingCISpec().getJobs()) {
			choices.add(job.getName());
		}
		choices.remove(belongingJob.getName());
		return choices;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> paramNames = new HashSet<>();
		boolean isValid = true;
		for (JobParam param: params) {
			if (BuildConstants.ALL_FIELDS.contains(param.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Reserved name: " + param.getName())
						.addPropertyNode("params").addConstraintViolation();
			} else if (paramNames.contains(param.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate param: " + param.getName())
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
