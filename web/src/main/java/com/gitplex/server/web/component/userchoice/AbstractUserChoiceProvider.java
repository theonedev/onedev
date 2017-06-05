package com.gitplex.server.web.component.userchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.component.select2.ChoiceProvider;
import com.gitplex.server.web.util.avatar.AvatarManager;
import com.google.common.collect.Lists;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<UserFacade> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(UserFacade choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getDisplayName()));
		String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<UserFacade> toChoices(Collection<String> ids) {
		List<UserFacade> users = Lists.newArrayList();
		CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			users.add(cacheManager.getUser(id));
		}

		return users;
	}

}