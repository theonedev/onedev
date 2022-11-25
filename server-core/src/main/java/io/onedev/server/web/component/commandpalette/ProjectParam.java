package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.PathCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.web.mapper.ProjectMapperUtils;

public class ProjectParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public ProjectParam(boolean optional) {
		super(ProjectMapperUtils.PARAM_PROJECT, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		ProjectQuery query;
		if (matchWith.length() == 0)
			query = new ProjectQuery();
		else
			query = new ProjectQuery(new PathCriteria("**/*" + matchWith + "*/**"));
		Map<String, String> suggestions = new LinkedHashMap<>();
		for (Project project: projectManager.query(query, 0, count))
			suggestions.put(project.getPath(), String.valueOf(project.getPath()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		try {
			if (projectManager.findFacadeByPath(matchWith) != null)
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
		
}
