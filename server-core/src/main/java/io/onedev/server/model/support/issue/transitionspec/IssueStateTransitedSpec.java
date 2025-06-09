package io.onedev.server.model.support.issue.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.OmitName;

@Editable(order=550, name="State of other issue is transited to")
public class IssueStateTransitedSpec extends AutoSpec {

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
	
	@Editable(order=9900, name="Applicable Issues", placeholder="All", description="Optionally specify issues "
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
	public String getTriggerDescription() {
		return MessageFormat.format(_T("state of other issue is transited to \"{0}\""), states);
	}
	
}
