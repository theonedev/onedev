package io.onedev.server.web.component.rolechoice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractRoleChoiceProvider extends ChoiceProvider<Role> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(Role choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getName()));
		if (choice.getDescription() != null)
			writer.key("description").value(HtmlEscape.escapeHtml5(choice.getDescription()));
	}

	@Override
	public Collection<Role> toChoices(Collection<String> ids) {
		List<Role> roles = Lists.newArrayList();
		RoleService roleService = OneDev.getInstance(RoleService.class);
		for (String each : ids) {
			Role role = roleService.load(Long.valueOf(each));
			Hibernate.initialize(role);
			roles.add(role);
		}

		return roles;	
	}

}