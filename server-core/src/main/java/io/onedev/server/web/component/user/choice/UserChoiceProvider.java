package io.onedev.server.web.component.user.choice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.model.User;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final List<User> choices;
	
	public UserChoiceProvider(List<User> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(User::getDisplayName));
	}
	
	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<User>() {

			@Override
			public double getMatchScore(User object) {
				return object.getMatchScore(term);
			}
			
		});
		
		new ResponseFiller<User>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}