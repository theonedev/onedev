package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entitytext.PullRequestTextManager;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;

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
		if (matchWith.length() == 0) {
			PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
			requests = pullRequestManager.query(project, new PullRequestQuery(), false, 0, count);
		} else {
			PullRequestTextManager pullRequestTextManager = OneDev.getInstance(PullRequestTextManager.class);
			requests = pullRequestTextManager.query(project, matchWith, false, 0, count);
		}
		for (PullRequest request: requests) {
			suggestions.put("#" + request.getNumber() + " - " + request.getTitle(), 
					String.valueOf(request.getNumber()));
		}
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
