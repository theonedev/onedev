package io.onedev.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.model.Group;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final List<Group> choices;
	
	public GroupChoiceProvider(List<Group> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(Group::getName));
	}
	
	@Override
	public void query(String term, int page, Response<Group> response) {
		List<Group> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<Group>() {

			@Override
			public double getMatchScore(Group object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<Group>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}