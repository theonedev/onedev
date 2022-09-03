package io.onedev.server.web.editable.buildspec.step;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.google.common.collect.Sets;

import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
abstract class StepEditPanel extends BeanEditModalPanel<Step> implements BuildSpecAware, ParamSpecAware {

	public StepEditPanel(AjaxRequestTarget target, Step step) {
		super(target, step, Sets.newHashSet(), true, EditableUtils.getGroupedType(step.getClass()));
	}

	@Nullable
	private static Step getStep(List<Step> steps, int stepIndex) {
		if (stepIndex != -1)
			return steps.get(stepIndex);
		else
			return null;
	}
	
	@Override
	protected void onCancel(AjaxRequestTarget target) {
	}

}
