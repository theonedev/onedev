package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Editable(order=400, name="Issue in state", description = "Job will run on head commit of default branch")
public class IssueInStateTrigger extends JobTrigger {

	private String state;

	private String applicableIssues;
	
	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	private static List<String> getStateChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting()
				.getStateSpecs().stream().map(StateSpec::getName).collect(toList());		
	}

	@Editable(order=800, placeholder = "Any issue")
	@io.onedev.server.annotation.IssueQuery(withProjectCriteria = false, withStateCriteria = false, withOrder = false)
	public String getApplicableIssues() {
		return applicableIssues;
	}

	public void setApplicableIssues(String applicableIssues) {
		this.applicableIssues = applicableIssues;
	}
	
	@Nullable
	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof IssueOpened) {
			IssueOpened issueOpened = (IssueOpened) event;
			var issue = issueOpened.getIssue();
			if (issue.getState().equals(state))
				return triggerMatches(issue);
			else 
				return null;
		} else if (event instanceof IssueChanged) {
			IssueChanged issueChanged = (IssueChanged) event;
			if (issueChanged.getChange().getData() instanceof IssueStateChangeData) {
				IssueStateChangeData changeData = (IssueStateChangeData) issueChanged.getChange().getData();
				if (changeData.getNewState().equals(state)) 
					return triggerMatches(issueChanged.getIssue());
				else 
					return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Nullable
	private TriggerMatch triggerMatches(Issue issue) {
		var options = new IssueQueryParseOption().withOrder(false);
		var query = IssueQuery.parse(issue.getProject(), getApplicableIssues(), options, true);
		if (query.matches(issue)) {
			var refName = GitUtils.branch2ref(issue.getProject().getDefaultBranch());
			return new TriggerMatch(refName, null, issue, getParams(),
					"Issue state is '" + issue.getState() + "'");
		} else {
			return null;
		}
	}
	
	@Override
	public String getTriggerDescription() {
		if (getApplicableIssues() != null)
			return "When issue is in state '" + getState() + "' and matches: " + getApplicableIssues();
		else
			return "When issue is in state '" + getState() + "'";
	}
	
}
