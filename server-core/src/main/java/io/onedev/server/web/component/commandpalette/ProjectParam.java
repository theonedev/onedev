package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.PathCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.mapper.ProjectMapperUtils;

public class ProjectParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public ProjectParam(boolean optional) {
		super(ProjectMapperUtils.PARAM_PROJECT, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		ProjectQuery query;
		if (matchWith.length() == 0)
			query = new ProjectQuery();
		else
			query = new ProjectQuery(new PathCriteria("**/*" + matchWith + "*/**", ProjectQueryLexer.Is));
		Map<String, String> suggestions = new LinkedHashMap<>();
		var subject = SecurityUtils.getSubject();
		for (Project project: projectService.query(subject, query, false, 0, count))
			suggestions.put(project.getPath(), String.valueOf(project.getPath()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		try {
			if (projectService.findFacadeByPath(matchWith) != null)
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
		
}
