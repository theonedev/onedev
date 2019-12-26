package io.onedev.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Group;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<Collection<Group>> choicesModel;
	
	public GroupChoiceProvider(IModel<Collection<Group>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Group> response) {
		List<Group> matched = MatchScoreUtils.filterAndSort(choicesModel.getObject(), new MatchScoreProvider<Group>() {

			@Override
			public double getMatchScore(Group object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<Group>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}