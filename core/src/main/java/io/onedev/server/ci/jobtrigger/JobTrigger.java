package io.onedev.server.ci.jobtrigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.ci.jobparam.JobParam;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean ignore;
	
	private List<JobParam> params = new ArrayList<>();

	@Editable(order=900, description="Check to ignore triggering of the job. This is normally used to "
			+ "exclude a specific condition from triggering the job, while there is a more general "
			+ "condition below to trigger the job")
	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	@Editable(order=1000)
	@ShowCondition("isParamsVisible")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}
	
	@SuppressWarnings("unused")
	private static boolean isParamsVisible() {
		return !(boolean) OneContext.get().getEditContext().getInputValue("ignore");
	}
	
	protected abstract boolean matches(ProjectEvent event, CISpec ciSpec, Job job);
	
	public abstract String getDescription();
	
}
