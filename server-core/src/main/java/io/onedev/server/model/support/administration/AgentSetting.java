package io.onedev.server.model.support.administration;

import static io.onedev.server.search.entity.agent.AgentQueryLexer.Is;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.NamedAgentQuery;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.search.entity.agent.NotUsedSinceCriteria;
import io.onedev.server.search.entity.agent.OsCriteria;

public class AgentSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedAgentQuery> namedQueries = new ArrayList<>();
	
	public AgentSetting() {
		namedQueries.add(new NamedAgentQuery("All agents", null));
		namedQueries.add(new NamedAgentQuery("Online agents", "online"));
		namedQueries.add(new NamedAgentQuery("Offline agents", "offline"));
		namedQueries.add(new NamedAgentQuery("Linux agents", new AgentQuery(new OsCriteria("Linux", Is)).toString()));
		namedQueries.add(new NamedAgentQuery("Windows agents", new AgentQuery(new OsCriteria("Windows", Is)).toString()));
		namedQueries.add(new NamedAgentQuery("Mac OS X agents", new AgentQuery(new OsCriteria("Mac OS X", Is)).toString()));
		namedQueries.add(new NamedAgentQuery("FreeBSD agents", new AgentQuery(new OsCriteria("FreeBSD", Is)).toString()));
		namedQueries.add(new NamedAgentQuery("Paused agents", "paused"));
		namedQueries.add(new NamedAgentQuery("Not used for 1 month", new AgentQuery(new NotUsedSinceCriteria("1 month ago")).toString()));
	}
	
	public List<NamedAgentQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedAgentQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedAgentQuery getNamedQuery(String name) {
		for (NamedAgentQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
