package io.onedev.server.ci.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.wicket.Component;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.ci.job.paramsupply.ParamSupply;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ParamSupply> params = new ArrayList<>();

	@Editable(name="Job Parameters", order=1000)
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
	
	public abstract boolean matches(ProjectEvent event, Job job);
	
	public abstract String getDescription();

}
