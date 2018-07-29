package io.onedev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.entityquery.issue.IssueQuery;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;

public class FirstIssueQueryLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public FirstIssueQueryLink(String id, Project project) {
		super(id, IssueListPage.class, getPageParameters(project));
	}

	private static PageParameters getPageParameters(Project project) {
		String query = null;
		List<String> queries = new ArrayList<>();
		if (project.getIssueQuerySettingOfCurrentUser() != null) { 
			for (NamedIssueQuery namedQuery: project.getIssueQuerySettingOfCurrentUser().getUserQueries())
				queries.add(namedQuery.getQuery());
		}
		for (NamedIssueQuery namedQuery: project.getSavedIssueQueries())
			queries.add(namedQuery.getQuery());
		for (String each: queries) {
			try {
				if (SecurityUtils.getUser() != null || !IssueQuery.parse(project, each, true).needsLogin()) {  
					query = each;
					break;
				}
			} catch (Exception e) {
			}
		} 
		return IssueListPage.paramsOf(project, query);
	}
	
}
