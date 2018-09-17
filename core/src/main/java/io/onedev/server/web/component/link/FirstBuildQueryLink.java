package io.onedev.server.web.component.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.BuildListPage;

public class FirstBuildQueryLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public FirstBuildQueryLink(String id, Project project) {
		super(id, BuildListPage.class, getPageParameters(project));
	}

	private static PageParameters getPageParameters(Project project) {
		String query = null;
		List<String> queries = new ArrayList<>();
		if (project.getBuildQuerySettingOfCurrentUser() != null) { 
			for (NamedBuildQuery namedQuery: project.getBuildQuerySettingOfCurrentUser().getUserQueries())
				queries.add(namedQuery.getQuery());
		}
		for (NamedBuildQuery namedQuery: project.getSavedBuildQueries())
			queries.add(namedQuery.getQuery());
		for (String each: queries) {
			try {
				if (SecurityUtils.getUser() != null || !BuildQuery.parse(project, each, true).needsLogin()) {  
					query = each;
					break;
				}
			} catch (Exception e) {
			}
		} 
		return BuildListPage.paramsOf(project, query);
	}
	
}
