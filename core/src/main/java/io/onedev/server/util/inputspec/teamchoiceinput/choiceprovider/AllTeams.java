package io.onedev.server.util.inputspec.teamchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="All teams")
public class AllTeams implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<TeamFacade> getChoices(boolean allPossible) {
		List<TeamFacade> teams = new ArrayList<>();
		for (TeamFacade team: OneDev.getInstance(CacheManager.class).getTeams().values()) {
			if (team.getProjectId().equals(OneContext.get().getProject().getId()))
				teams.add(team);
		}
		return teams;
	}

}
