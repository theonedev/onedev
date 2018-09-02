package io.onedev.server.web.component.teamchoice;

import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractTeamChoiceProvider extends ChoiceProvider<TeamFacade> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(TeamFacade choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(choice.getName());
	}

	@Override
	public Collection<TeamFacade> toChoices(Collection<String> ids) {
		List<TeamFacade> teams = Lists.newArrayList();
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			teams.add(cacheManager.getTeam(id));
		}

		return teams;
	}

}