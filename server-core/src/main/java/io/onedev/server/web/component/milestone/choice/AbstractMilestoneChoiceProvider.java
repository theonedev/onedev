package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractMilestoneChoiceProvider extends ChoiceProvider<Milestone> {

	private static final long serialVersionUID = 1L;
	
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
	
}