package io.onedev.server.web.component.commandpalette;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.manager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.search.entity.agent.NameCriteria;
import io.onedev.server.search.entity.agent.OsArchCriteria;
import io.onedev.server.search.entity.agent.OsCriteria;
import io.onedev.server.search.entity.agent.OsVersionCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentDetailPage;

public class AgentParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public AgentParam(boolean optional) {
		super(AgentDetailPage.PARAM_AGENT, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		AgentManager agentManager = OneDev.getInstance(AgentManager.class);
		AgentQuery query;
		if (matchWith.length() == 0) {
			query = new AgentQuery();
		} else {
			List<Criteria<Agent>> criterias = new ArrayList<>();
			criterias.add(new NameCriteria("*" + matchWith + "*"));
			criterias.add(new OsCriteria("*" + matchWith + "*"));
			criterias.add(new OsVersionCriteria("*" + matchWith + "*"));
			criterias.add(new OsArchCriteria("*" + matchWith + "*"));
			query = new AgentQuery(new OrCriteria<Agent>(criterias));
		}
		
		for (Agent agent: agentManager.query(query, 0, count))
			suggestions.put(agent.getName(), agent.getName());
		
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		AgentManager agentManager = OneDev.getInstance(AgentManager.class);
		return agentManager.findByName(matchWith) != null; 
	}
		
}
