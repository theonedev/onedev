package io.onedev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.model.support.pullrequest.query.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.pullrequests.requestlist.RequestListPage;

public class FirstPullRequestQueryLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public FirstPullRequestQueryLink(String id, Project project) {
		super(id, RequestListPage.class, getPageParameters(project));
	}

	private static PageParameters getPageParameters(Project project) {
		String query = null;
		List<String> queries = new ArrayList<>();
		if (project.getPullRequestQuerySettingOfCurrentUser() != null) { 
			for (NamedPullRequestQuery namedQuery: project.getPullRequestQuerySettingOfCurrentUser().getUserQueries())
				queries.add(namedQuery.getQuery());
		}
		for (NamedPullRequestQuery namedQuery: project.getSavedPullRequestQueries())
			queries.add(namedQuery.getQuery());
		for (String each: queries) {
			try {
				if (SecurityUtils.getUser() != null || !PullRequestQuery.parse(project, each, true).needsLogin()) {  
					query = each;
					break;
				}
			} catch (Exception e) {
			}
		} 
		return RequestListPage.paramsOf(project, query);
	}
	
}
