package io.onedev.server.model.support.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class DefaultFixedIssueFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobNames;
	
	private String issueQuery;

	@Editable(order=100, description="Specify space-separated jobs. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestJobNames")
	@NotEmpty
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}

	@Editable(order=200, description="Specify a default query to filter/order fixed issues of specified jobs")
	@IssueQuery(withCurrentBuildCriteria = false, withCurrentCommitCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentUserCriteria = true, 
			withOrder = true)
	@NotEmpty
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobNames(String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class).getJobNames(Project.get()));
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}
	
}
