package io.onedev.server.web.component.commandpalette;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.JobCriteria;
import io.onedev.server.search.entity.build.VersionCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

public class BuildParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public BuildParam(boolean optional) {
		super(BuildDetailPage.PARAM_BUILD, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		BuildQuery query;
		if (matchWith.length() == 0) {
			query = new BuildQuery();
		} else {		
			List<Criteria<Build>> criterias = new ArrayList<>();
			criterias.add(new VersionCriteria("*" + matchWith + "*"));
			criterias.add(new JobCriteria("*" + matchWith + "*"));
			query = new BuildQuery(new OrCriteria<Build>(criterias));
		}
		List<Build> builds = buildManager.query(ParsedUrl.getProject(paramValues), query, 0, count);
		
		for (Build build: builds) {
			if (build.getVersion() != null) {
				suggestions.put("#" + build.getNumber() + " (" + build.getVersion() + ") - " + build.getJobName(), 
						String.valueOf(build.getNumber()));
			} else {
				suggestions.put("#" + build.getNumber() + " - " + build.getJobName(), 
						String.valueOf(build.getNumber()));
			}
		}
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
