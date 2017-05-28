package com.gitplex.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.web.component.select2.ChoiceProvider;

public abstract class AbstractGroupChoiceProvider extends ChoiceProvider<Group> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(Group choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(choice.getName());
	}

	@Override
	public Collection<Group> toChoices(Collection<String> ids) {
		List<Group> groups = Lists.newArrayList();
		GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			groups.add(groupManager.load(id));
		}

		return groups;
	}

}