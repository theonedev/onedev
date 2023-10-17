package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.OmitName;

@Editable(order=550, name="State of other issue is transited to")
public class StateTransitionTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private List<String> states = new ArrayList<>();
	
	@Editable(order=100)
	@OmitName
	@ChoiceProvider("getStateChoices")
	@Size(min=1, message="At least one state needs to be specified")
	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states;
	}

	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpecs())
			stateNames.add(state.getName());
		return stateNames;
	}
	
	@Editable(order=1000, name="Applicable Issues", placeholder="All", description="Optionally specify issues "
			+ "applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentIssueCriteria = true)
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@Override
	public String getDescription() {
		return "state of other issue is transited to " + states;
	}
	
}
