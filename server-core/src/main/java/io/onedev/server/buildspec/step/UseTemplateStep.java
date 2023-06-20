package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.VariableOption;

@Editable(order=10000, name="Use Step Template", description="Run specified step template")
public class UseTemplateStep extends Step {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_TEMPLATE_NAME = "templateName";
	
	private String templateName;
	
	private List<ParamSupply> params = new ArrayList<>();

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

	@Editable(name="Step Parameters", order=200)
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=true, withDynamicVariables=true)
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
		String templateName = (String) EditContext.get().getInputValue(PROP_TEMPLATE_NAME);
		if (templateName != null) {
			BuildSpec buildSpec = BuildSpec.get();	
			StepTemplate template = buildSpec.getStepTemplateMap().get(templateName);
			if (template != null) 
				return template.getParamSpecs();
		}		
		return new ArrayList<>();
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		StepTemplate template = build.getSpec().getStepTemplateMap().get(templateName);
		if (template == null)
			throw new ExplicitException("Step template not found: " + templateName);
		
		List<Action> actions = new ArrayList<>();
		AtomicInteger repeatRef = new AtomicInteger(0);
		new MatrixRunner<List<String>>(ParamUtils.getParamMatrix(build, paramCombination, getParams())) {
			
			@Override
			public void run(Map<String, List<String>> paramMap) {
				int repeat = repeatRef.incrementAndGet();
				ParamUtils.validateParamMap(template.getParamSpecs(), paramMap);
				
				ParamCombination newParamCombination = new ParamCombination(template.getParamSpecs(), paramMap);
				VariableInterpolator interpolator = new VariableInterpolator(build, newParamCombination);
				for (Step step: template.getSteps()) {
					step = interpolator.interpolateProperties(step);
					String actionName = step.getName();
					if (repeat != 1) 
						actionName += " (" + repeat + ")";
					actions.add(step.getAction(actionName, build, jobExecutor, jobToken, newParamCombination));
				}
			}
			
		}.run();
		
		return new CompositeFacade(actions);
	}

}
