package io.onedev.server.web.component.user.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getDisplayName().trim()));
		String avatarUrl = OneDev.getInstance(AvatarService.class).getUserAvatarUrl(choice.getId());
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		UserService userService = OneDev.getInstance(UserService.class);
		for (String each : ids) {
			User user = userService.load(Long.valueOf(each)); 
			Hibernate.initialize(user);
			users.add(user);
		}

		return users;
	}

}