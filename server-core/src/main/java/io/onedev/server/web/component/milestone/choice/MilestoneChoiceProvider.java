package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class MilestoneChoiceProvider extends ChoiceProvider<Milestone> {

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
	public void toJson(Milestone choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getName()));
		writer.key("statusName").value(choice.getStatusName());
		if (choice.isClosed()) 
			writer.key("statusClass").value("badge-success");
		else
			writer.key("statusClass").value("badge-warning");
	}

	@Override
	public Collection<Milestone> toChoices(Collection<String> ids) {
		List<Milestone> milestones = Lists.newArrayList();
		MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
		for (String each : ids) {
			Milestone milestone = milestoneManager.load(Long.valueOf(each));
			Hibernate.initialize(milestone);
			milestones.add(milestone);
		}

		return milestones;
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