package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.action.RunJobAction;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.UseTemplateStep;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.buildspec.job.jobdependency.JobDependencyEditPanel;
import io.onedev.server.web.editable.buildspec.job.postbuildaction.PostBuildActionEditPanel;
import io.onedev.server.web.editable.buildspec.job.trigger.JobTriggerEditPanel;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class ParamMap implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ParamInstance> params = new ArrayList<>();

	@Editable
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withDynamicVariables=false)
	@OmitName
	@Valid
	public List<ParamInstance> getParams() {
		return params;
	}

	public void setParams(List<ParamInstance> params) {
		this.params = params;
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		Component component = ComponentContext.get().getComponent();
		if (WicketUtils.findInnermost(component, JobTriggerEditPanel.class) != null)
			return JobTrigger.getParamSpecs();
		else if (WicketUtils.findInnermost(component, PostBuildActionEditPanel.class) != null)
			return RunJobAction.getParamSpecs();
		else if (WicketUtils.findInnermost(component, JobDependencyEditPanel.class) != null)
			return JobDependency.getParamSpecs();
		else 
			return UseTemplateStep.getParamSpecs();
	}
	
}
