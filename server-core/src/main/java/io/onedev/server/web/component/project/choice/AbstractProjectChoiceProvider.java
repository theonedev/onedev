package io.onedev.server.web.component.project.choice;

import java.util.Collection;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.component.select2.ChoiceProvider;

@SuppressWarnings("serial")
public abstract class AbstractProjectChoiceProvider extends ChoiceProvider<Project> {

	@Override
	public void toJson(Project choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		writer.value(HtmlEscape.escapeHtml5(choice.getName()));
		String avatarUrl = OneDev.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}
	
	@Override
	public Collection<Project> toChoices(Collection<String> ids) {
		return ids.stream()
				.map(it-> {
					Project project = OneDev.getInstance(ProjectManager.class).load(Long.valueOf(it));
					Hibernate.initialize(project);
					return project;
					}
				).collect(Collectors.toList());
	}

}
