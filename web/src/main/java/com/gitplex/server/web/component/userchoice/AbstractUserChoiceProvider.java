package com.gitplex.server.web.component.userchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.web.component.select2.ChoiceProvider;
import com.gitplex.server.web.util.avatar.AvatarManager;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getDisplayName()));
		String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		UserManager userManager = GitPlex.getInstance(UserManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			users.add(userManager.load(id));
		}

		return users;
	}

}