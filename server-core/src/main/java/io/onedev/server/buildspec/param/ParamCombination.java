package io.onedev.server.buildspec.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspecmodel.inputspec.Input;

public class ParamCombination implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<ParamSpec> paramSpecs;
	
	private final Map<String, List<String>> paramMap;
	
	private final Map<String, Input> paramInputs;
	
	private transient Map<String, ParamSpec> paramSpecMap;
	
	public ParamCombination(List<ParamSpec> paramSpecs, Map<String, List<String>> paramMap) {
		this.paramSpecs = paramSpecs;
		this.paramMap = paramMap;
		
		paramInputs = new LinkedHashMap<>();
		for (ParamSpec paramSpec: paramSpecs) {
			String paramName = paramSpec.getName();
			List<String> paramValues = paramMap.get(paramName);
			if (paramValues != null) {
				List<String> normalizedValues = new ArrayList<>();
				for (String paramValue: paramValues) {
					if (paramValue != null)
						normalizedValues.add(paramValue);
				}
				Collections.sort(normalizedValues, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return (int) (paramSpec.getOrdinal(o1) - paramSpec.getOrdinal(o2));
					}
					
				});
				if (!paramSpec.isAllowMultiple() && normalizedValues.size() > 1) 
					normalizedValues = Lists.newArrayList(normalizedValues.iterator().next());
				paramInputs.put(paramName, new Input(paramName, paramSpec.getType(), normalizedValues));
			}
		}		
	}
	
	public Map<String, ParamSpec> getParamSpecMap() {
		if (paramSpecMap == null) 
			paramSpecMap = ParamUtils.getParamSpecMap(paramSpecs);
		return paramSpecMap;
	}
	
	public Map<String, Input> getParamInputs() {
		return paramInputs;
	}
	
	public Map<String, List<String>> getParamMap() {
		return paramMap;
	}
	
	public boolean isParamVisible(String paramName) {
		return isParamVisible(paramName, Sets.newHashSet());
	}
	
	private boolean isParamVisible(String paramName, Set<String> checkedParamNames) {
		if (!checkedParamNames.add(paramName))
			return false;
		
		ParamSpec paramSpec = Preconditions.checkNotNull(getParamSpecMap().get(paramName));
		if (paramSpec.getShowCondition() != null) {
			Input dependentInput = paramInputs.get(paramSpec.getShowCondition().getInputName());
			Preconditions.checkNotNull(dependentInput);
			if (paramSpec.getShowCondition().getValueMatcher().matches(dependentInput.getValues())) 
				return isParamVisible(dependentInput.getName(), checkedParamNames);
			else 
				return false;
		} else {
			return true;
		}
	}
		
}
