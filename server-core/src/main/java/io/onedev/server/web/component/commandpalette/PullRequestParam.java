package io.onedev.server.web.component.commandpalette;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.pullrequest.FuzzyCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser;
import io.onedev.server.search.entity.pullrequest.ReferenceCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PullRequestParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public PullRequestParam(boolean optional) {
		super(PullRequestDetailPage.PARAM_REQUEST, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		Project project = ParsedUrl.getProject(paramValues);
		List<PullRequest> requests;
		PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
		if (matchWith.length() == 0) {
			requests = pullRequestManager.query(project, new PullRequestQuery(), false, 0, count);
		} else {
			Criteria<PullRequest> criteria;
			try {
				criteria = new ReferenceCriteria(project, matchWith, PullRequestQueryParser.Is);
			} catch (Exception e) {
				criteria = new FuzzyCriteria(matchWith);	
			}
			requests = pullRequestManager.query(project, new PullRequestQuery(criteria), false, 0, count);
		}
		for (PullRequest request: requests) 
			suggestions.put(request.getSummary(project), String.valueOf(request.getNumber()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
		try {
			Long requestNumber = Long.valueOf(matchWith);
			if (pullRequestManager.find(ParsedUrl.getProject(paramValues), requestNumber) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
