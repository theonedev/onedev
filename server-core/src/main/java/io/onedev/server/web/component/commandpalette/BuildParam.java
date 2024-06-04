package io.onedev.server.web.component.commandpalette;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.BuildQueryParser;
import io.onedev.server.search.entity.build.FuzzyCriteria;
import io.onedev.server.search.entity.build.ReferenceCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuildParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public BuildParam(boolean optional) {
		super(BuildDetailPage.PARAM_BUILD, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		var project = ParsedUrl.getProject(paramValues);
		Map<String, String> suggestions = new LinkedHashMap<>();
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		BuildQuery query;
		if (matchWith.length() == 0) {
			query = new BuildQuery();
		} else {	
			Criteria<Build> criteria;
			try {
				criteria = new ReferenceCriteria(project, matchWith, BuildQueryParser.Is);
			} catch (Exception e) {
				criteria = new FuzzyCriteria(matchWith);
			}
			query = new BuildQuery(criteria);
		}
		List<Build> builds = buildManager.query(project, query, false, 0, count);
		
		for (Build build: builds) 
			suggestions.put(build.getSummary(project), String.valueOf(build.getNumber()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		try {
			Long buildNumber = Long.valueOf(matchWith);
			if (buildManager.find(ParsedUrl.getProject(paramValues), buildNumber) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
