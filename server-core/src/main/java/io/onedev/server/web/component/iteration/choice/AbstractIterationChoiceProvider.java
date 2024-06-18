package io.onedev.server.web.component.iteration.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.model.Iteration;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractIterationChoiceProvider extends ChoiceProvider<Iteration> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(Iteration choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getName()));
		writer.key("statusName").value(choice.getStatusName());
		if (choice.isClosed()) 
			writer.key("statusClass").value("badge-success");
		else
			writer.key("statusClass").value("badge-warning");
	}

	@Override
	public Collection<Iteration> toChoices(Collection<String> ids) {
		List<Iteration> iterations = Lists.newArrayList();
		IterationManager iterationManager = OneDev.getInstance(IterationManager.class);
		for (String each : ids) {
			Iteration iteration = iterationManager.load(Long.valueOf(each));
			Hibernate.initialize(iteration);
			iterations.add(iteration);
		}

		return iterations;
	}
	
}