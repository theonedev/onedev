package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.Similarities;
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
		List<Milestone> milestones = new Similarities<Milestone>(choicesModel.getObject()) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(Milestone object) {
				return Similarities.getSimilarScore(object.getName(), term);
			}
			
		};
		new ResponseFiller<Milestone>(response).fill(milestones, page, WebConstants.PAGE_SIZE);
	}

}