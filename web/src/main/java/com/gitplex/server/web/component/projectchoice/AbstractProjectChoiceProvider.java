package com.gitplex.server.web.component.projectchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.component.select2.ChoiceProvider;

@SuppressWarnings("serial")
public abstract class AbstractProjectChoiceProvider extends ChoiceProvider<Project> {

	@Override
	public void toJson(Project choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		writer.value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}
	
	@Override
	public Collection<Project> toChoices(Collection<String> ids) {
		List<Project> list = Lists.newArrayList();
		ProjectManager projectManager = GitPlex.getInstance(ProjectManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(projectManager.load(id));
		}
		
		return list;
	}

}
