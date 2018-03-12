package io.onedev.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final List<GroupFacade> choices;
	
	public GroupChoiceProvider(List<GroupFacade> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(GroupFacade::getName));
	}
	
	@Override
	public void query(String term, int page, Response<GroupFacade> response) {
		List<GroupFacade> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<GroupFacade>() {

			@Override
			public double getMatchScore(GroupFacade object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<GroupFacade>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}