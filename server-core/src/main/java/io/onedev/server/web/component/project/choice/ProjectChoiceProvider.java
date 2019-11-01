package io.onedev.server.web.component.project.choice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class ProjectChoiceProvider extends AbstractProjectChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final List<Project> choices;
	
	public ProjectChoiceProvider(List<Project> choices) {
		this.choices = new ArrayList<>(choices);
		this.choices.sort(Comparator.comparing(Project::getName));
	}
	
	@Override
	public void query(String term, int page, Response<Project> response) {
		List<Project> matched = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<Project>() {

			@Override
			public double getMatchScore(Project object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<Project>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}

}