package io.onedev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.codecomment.NamedCodeCommentQuery;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.comments.ProjectCodeCommentsPage;

public class FirstCodeCommentQueryLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public FirstCodeCommentQueryLink(String id, Project project) {
		super(id, ProjectCodeCommentsPage.class, getPageParameters(project));
	}

	private static PageParameters getPageParameters(Project project) {
		String query = null;
		List<String> queries = new ArrayList<>();
		if (project.getCodeCommentQuerySettingOfCurrentUser() != null) { 
			for (NamedCodeCommentQuery namedQuery: project.getCodeCommentQuerySettingOfCurrentUser().getUserQueries())
				queries.add(namedQuery.getQuery());
		}
		for (NamedCodeCommentQuery namedQuery: project.getSavedCodeCommentQueries())
			queries.add(namedQuery.getQuery());
		for (String each: queries) {
			try {
				if (SecurityUtils.getUser() != null || !CodeCommentQuery.parse(project, each, true).needsLogin()) {  
					query = each;
					break;
				}
			} catch (Exception e) {
			}
		} 
		return ProjectCodeCommentsPage.paramsOf(project, query);
	}
	
}
