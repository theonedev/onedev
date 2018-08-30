package io.onedev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.search.commit.CommitQueryUtils;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;

public class FirstCommitQueryLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public FirstCommitQueryLink(String id, Project project) {
		super(id, ProjectCommitsPage.class, getPageParameters(project));
	}

	private static PageParameters getPageParameters(Project project) {
		String query = null;
		List<String> queries = new ArrayList<>();
		if (project.getCommitQuerySettingOfCurrentUser() != null) { 
			for (NamedCommitQuery namedQuery: project.getCommitQuerySettingOfCurrentUser().getUserQueries())
				queries.add(namedQuery.getQuery());
		}
		for (NamedCommitQuery namedQuery: project.getSavedCommitQueries())
			queries.add(namedQuery.getQuery());
		for (String each: queries) {
			try {
				if (SecurityUtils.getUser() != null || !CommitQueryUtils.needsLogin(each)) {  
					query = each;
					break;
				}
			} catch (Exception e) {
			}
		} 
		ProjectCommitsPage.State state = new ProjectCommitsPage.State();
		state.query = query;
		return ProjectCommitsPage.paramsOf(project, state);
	}
	
}
