package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class MilestoneChoiceProvider extends AbstractMilestoneChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Collection<Milestone>> choicesModel;
	
	public MilestoneChoiceProvider(IModel<Collection<Milestone>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Milestone> response) {
		List<Milestone> milestones = MatchScoreUtils.filterAndSort(choicesModel.getObject(), 
				new MatchScoreProvider<Milestone>() {

			@Override
			public double getMatchScore(Milestone object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		new ResponseFiller<Milestone>(response).fill(milestones, page, WebConstants.PAGE_SIZE);
	}

}