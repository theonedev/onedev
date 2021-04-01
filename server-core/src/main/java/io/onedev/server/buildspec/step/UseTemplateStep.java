package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CompositeExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=200, name="Use Step Template", description="Step to use specified template")
public class UseTemplateStep extends Step {

	private static final long serialVersionUID = 1L;

	private String templateName;
	
	private List<ParamSupply> params = new ArrayList<>();

	@Editable(order=100)
	@NotEmpty
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Editable(name="Step Parameters", order=200)
	@ParamSpecProvider("getParamSpecs")
	@OmitName
	@Valid
	public List<ParamSupply> getParams() {
		return params;
	}

	public void setParams(List<ParamSupply> params) {
		this.params = params;
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		Component component = ComponentContext.get().getComponent();
		JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
		if (jobAware != null) {
			Job job = jobAware.getJob();
			if (job != null)
				return job.getParamSpecs();
		}
		return new ArrayList<>();
	}
	
	@Override
	public Executable getExecutable(BuildSpec buildSpec) {
		StepTemplate template = buildSpec.getStepTemplateMap().get(templateName);
		if (template == null)
			throw new ExplicitException("Step template not found: " + templateName);
		List<Action> actions = new ArrayList<>();
		for (Step step: template.getSteps())
			actions.add(step.getAction(buildSpec));
		return new CompositeExecutable(actions);
	}

}
