package io.onedev.server.web.component.user.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.User;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Collection<User>> choicesModel;
	
	public UserChoiceProvider(IModel<Collection<User>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> matched = MatchScoreUtils.filterAndSort(choicesModel.getObject(), new MatchScoreProvider<User>() {

			@Override
			public double getMatchScore(User object) {
				return object.getMatchScore(term);
			}
			
		});
		
		new ResponseFiller<User>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}