package io.onedev.server.buildspec.step;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.Action;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.ParamMap;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.web.editable.BeanEditor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.onedev.server.buildspec.param.ParamUtils.resolveParams;

@Editable(order=10000, name="Use Step Template", description="Run specified step template")
public class UseTemplateStep extends CompositeStep {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_TEMPLATE_NAME = "templateName";
	
	private String templateName;

	private List<ParamInstances> paramMatrix = new ArrayList<>();
	
	private List<ParamMap> excludeParamMaps = new ArrayList<>();

	@Editable(order=100)
	@ChoiceProvider("getTemplateChoices")
	@NotEmpty
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getTemplateChoices() {
		return new ArrayList<>(BuildSpec.get().getStepTemplateMap().keySet());
	}

	@Editable(order=200)
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withDynamicVariables=false)
	@OmitName
	@Valid
	public List<ParamInstances> getParamMatrix() {
		return paramMatrix;
	}

	public void setParamMatrix(List<ParamInstances> paramMatrix) {
		this.paramMatrix = paramMatrix;
	}

	@Editable(order=300, name="Exclude Param Combos")
	@ShowCondition("isExcludeParamMapsVisible")
	@Valid
	public List<ParamMap> getExcludeParamMaps() {
		return excludeParamMaps;
	}

	public void setExcludeParamMaps(List<ParamMap> excludeParamMaps) {
		this.excludeParamMaps = excludeParamMaps;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static boolean isExcludeParamMapsVisible() {
		var componentContext = ComponentContext.get();
		if (componentContext != null && componentContext.getComponent().findParent(BeanEditor.class) != null) {
			return !getParamSpecs().isEmpty();
		} else {
			var excludeParamMaps = (List<ParamMap>) EditContext.get().getInputValue("excludeParamMaps");
			return !excludeParamMaps.isEmpty();
		}
	}
	
	public static List<ParamSpec> getParamSpecs() {
		String templateName = (String) EditContext.get().getInputValue(PROP_TEMPLATE_NAME);
		if (templateName != null) {
			BuildSpec buildSpec = BuildSpec.get();	
			if (buildSpec != null) {
				StepTemplate template = buildSpec.getStepTemplateMap().get(templateName);
				if (template != null)
					return template.getParamSpecs();
			}
		}		
		return new ArrayList<>();
	}
	
	@Override
	protected List<Action> getActions(Build build, JobExecutor jobExecutor, String jobToken, 
								ParamCombination paramCombination) {
		StepTemplate template = build.getSpec().getStepTemplateMap().get(templateName);
		if (template == null)
			throw new ExplicitException("Step template not found: " + templateName);
		
		List<Action> actions = new ArrayList<>();
		AtomicInteger repeatRef = new AtomicInteger(0);
		var paramMaps = resolveParams(build, paramCombination, getParamMatrix(), getExcludeParamMaps());
		for (var paramMap: paramMaps) {
			int repeat = repeatRef.incrementAndGet();
			ParamUtils.validateParamMap(template.getParamSpecs(), paramMap);

			ParamCombination newParamCombination = new ParamCombination(template.getParamSpecs(), paramMap);
			VariableInterpolator interpolator = new VariableInterpolator(build, newParamCombination);
			for (Step step : template.getSteps()) {
				step = interpolator.interpolateProperties(step);
				String actionName = step.getName();
				if (repeat != 1)
					actionName += " (" + repeat + ")";
				actions.add(step.getAction(actionName, build, jobExecutor, jobToken, newParamCombination));
			}
		}
		return actions;
	}

	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		var template = build.getSpec().getStepTemplateMap().get(getTemplateName());
		if (template != null) {
			for (var step: template.getSteps()) {
				if (!step.isApplicable(build, executor))
					return false;
			}
			return true;
		} else {
			return true;
		}
	}

}
