package io.onedev.server.web.component.user.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getDisplayName()));
		String avatarUrl = OneDev.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		UserManager userManager = OneDev.getInstance(UserManager.class);
		for (String each : ids) {
			User user = userManager.load(Long.valueOf(each)); 
			Hibernate.initialize(user);
			users.add(user);
		}

		return users;
	}

}