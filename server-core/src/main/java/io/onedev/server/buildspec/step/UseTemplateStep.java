package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CompositeExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.model.Build;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;

@Editable(order=200, name="Use Step Template", description="Step to use specified template")
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
		List<String> templateNames = new ArrayList<>(BuildSpec.get().getStepTemplateMap().keySet());
		
		EditContext editContext = EditContext.get(1);
		BeanEditor beanEditor = ((Component) editContext).findParent(BeanEditor.class);
		if (beanEditor.getDescriptor().getBeanClass() == StepTemplate.class)
			templateNames.remove(editContext.getInputValue("name"));
		return templateNames;
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
	public Executable getExecutable(Build build, ParamCombination paramCombination) {
		StepTemplate template = build.getSpec().getStepTemplateMap().get(templateName);
		if (template == null)
			throw new ExplicitException("Step template not found: " + templateName);
		
		List<Action> actions = new ArrayList<>();
		new MatrixRunner<List<String>>(ParamUtils.getParamMatrix(build, paramCombination, getParams())) {
			
			@Override
			public void run(Map<String, List<String>> paramMap) {
				ParamUtils.validateParamMap(template.getParamSpecs(), paramMap);
				
				ParamCombination newParamCombination = new ParamCombination(template.getParamSpecs(), paramMap);
				VariableInterpolator interpolator = new VariableInterpolator(build, newParamCombination);
				for (Step step: template.getSteps()) {
					step = interpolator.interpolateProperties(step);
					actions.add(step.getAction(build, newParamCombination));
				}
			}
			
		}.run();
		
		return new CompositeExecutable(actions);
	}

}
