package io.onedev.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractGroupChoiceProvider extends ChoiceProvider<Group> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(Group choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getName()));
	}

	@Override
	public Collection<Group> toChoices(Collection<String> ids) {
		List<Group> groups = Lists.newArrayList();
		GroupManager groupManager = OneDev.getInstance(GroupManager.class);
		for (String each : ids) {
			Group group = groupManager.load(Long.valueOf(each));
			Hibernate.initialize(group);
			groups.add(group);
		}

		return groups;
	}

}