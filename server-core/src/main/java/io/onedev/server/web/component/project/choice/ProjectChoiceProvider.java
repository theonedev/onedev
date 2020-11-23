package io.onedev.server.web.component.project.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class ProjectChoiceProvider extends AbstractProjectChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Collection<Project>> choicesModel;
	
	public ProjectChoiceProvider(IModel<Collection<Project>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Project> response) {
		List<Project> matched = MatchScoreUtils.filterAndSort(choicesModel.getObject(), new MatchScoreProvider<Project>() {

			@Override
			public double getMatchScore(Project object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<Project>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}