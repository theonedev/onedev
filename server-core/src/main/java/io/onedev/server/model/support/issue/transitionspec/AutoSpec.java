package io.onedev.server.model.support.issue.transitionspec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Editable
public abstract class AutoSpec extends TransitionSpec {
	
	private String toState;

	@Editable(order=50)
	@NotEmpty
	@ChoiceProvider("getStateChoices")
	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	@Override
	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = super.getUndefinedStates();
		if (getIssueSetting().getStateSpec(toState) == null)
			undefinedStates.add(toState);
		return undefinedStates;
	}

	@Override
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		if (!super.fixUndefinedStates(resolutions))
			return false;
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) {
				if (toState.equals(entry.getKey()))
					toState = entry.getValue().getNewState();
			} else if (toState.equals(entry.getKey())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<String> getToStates() {
		return Lists.newArrayList(getToState());
	}

}
