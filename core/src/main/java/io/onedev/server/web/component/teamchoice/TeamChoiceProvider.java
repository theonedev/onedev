package io.onedev.server.web.component.teamchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class TeamChoiceProvider extends AbstractTeamChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final List<TeamFacade> choices;
	
	public TeamChoiceProvider(List<TeamFacade> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(TeamFacade::getName));
	}
	
	@Override
	public void query(String term, int page, Response<TeamFacade> response) {
		List<TeamFacade> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<TeamFacade>() {

			@Override
			public double getMatchScore(TeamFacade object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<TeamFacade>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}