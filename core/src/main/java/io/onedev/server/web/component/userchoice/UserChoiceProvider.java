package io.onedev.server.web.component.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final List<UserFacade> choices;
	
	public UserChoiceProvider(List<UserFacade> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(UserFacade::getDisplayName));
	}
	
	@Override
	public void query(String term, int page, Response<UserFacade> response) {
		List<UserFacade> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<UserFacade>() {

			@Override
			public double getMatchScore(UserFacade object) {
				return object.getMatchScore(term);
			}
			
		});
		
		new ResponseFiller<UserFacade>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}