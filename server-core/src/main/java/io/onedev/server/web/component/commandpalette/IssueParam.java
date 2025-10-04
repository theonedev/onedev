package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.issue.FuzzyCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParser;
import io.onedev.server.search.entity.issue.ReferenceCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;

public class IssueParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public IssueParam(boolean optional) {
		super(IssueDetailPage.PARAM_ISSUE, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		List<Issue> issues;
		var project = ParsedUrl.getProject(paramValues);
		ProjectScope projectScope = new ProjectScope(project, false, false);
		IssueService issueService = OneDev.getInstance(IssueService.class);
		var subject = SecurityUtils.getSubject();
		if (matchWith.length() == 0) {
			issues = issueService.query(subject, projectScope, new IssueQuery(), false, 0, count);
		} else {
			Criteria<Issue> criteria;
			try {
				criteria = new ReferenceCriteria(project, matchWith, IssueQueryParser.Is);
			} catch (Exception e) {
				criteria = new FuzzyCriteria(matchWith);
			}
			issues = issueService.query(subject, projectScope, new IssueQuery(criteria), false, 0, count);
		}
		for (Issue issue: issues) 
			suggestions.put(issue.getSummary(project), String.valueOf(issue.getNumber()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		IssueService issueService = OneDev.getInstance(IssueService.class);
		try {
			Long issueNumber = Long.valueOf(matchWith);
			if (issueService.find(ParsedUrl.getProject(paramValues), issueNumber) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
