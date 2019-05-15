package io.onedev.server.ci.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobParam> params = new ArrayList<>();

	@Editable(name="Trigger Parameters", order=1000, description="Specify parameters to trigger the job")
	@ParamSpecProvider("getParamSpecs")
	@OmitName
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSpec> getParamSpecs() {
		Component component = OneContext.get().getComponent();
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
