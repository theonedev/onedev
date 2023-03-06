package io.onedev.server.buildspec.param.spec;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;

import io.onedev.server.annotation.Multiline;
import org.apache.wicket.Component;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.annotation.ParamName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class ParamSpec extends InputSpec {
	
	private static final long serialVersionUID = 1L;
	
	@Editable(order=10)
	@ParamName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Editable(order=30, placeholder="No description", description="Optionally describes the param. Html tags are accepted.")
	@Multiline
	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

	@Editable(order=35, description="Whether or not multiple values can be specified for this param")
	@Override
	public boolean isAllowMultiple() {
		return super.isAllowMultiple();
	}

	@Override
	public void setAllowMultiple(boolean allowMultiple) {
		super.setAllowMultiple(allowMultiple);
	}

	@Editable(order=40, name="Show Conditionally", placeholder="Always", 
			description="Enable if visibility of this param depends on other params")
	@Valid
	@Override
	public ShowCondition getShowCondition() {
		return super.getShowCondition();
	}

	@Override
	public void setShowCondition(ShowCondition showCondition) {
		super.setShowCondition(showCondition);
	}
	
	@Editable(order=50, name="Allow Empty Value", description="Whether or not this param accepts empty value")
	@Override
	public boolean isAllowEmpty() {
		return super.isAllowEmpty();
	}

	@Override
	public void setAllowEmpty(boolean allowEmpty) {
		super.setAllowEmpty(allowEmpty);
	}

	@Override
	public String getNameOfEmptyValue() {
		return null;
	}

	@Nullable
	public static List<ParamSpec> list() {
		Component component = ComponentContext.get().getComponent();
		ParamSpecAware paramSpecAware = WicketUtils.findInnermost(component, ParamSpecAware.class);
		if (paramSpecAware != null) 
			return paramSpecAware.getParamSpecs();
		else
			return null;
	}
	
}
