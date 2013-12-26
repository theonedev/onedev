package com.pmease.gitop.web.component.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.GitopWebApp;
import com.pmease.gitop.web.component.avatar.AvatarImageResourceReference;
import com.pmease.gitop.web.util.Gravatar;
import com.pmease.gitop.web.util.WicketUtils;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class UserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<User> response) {
		if (Strings.isNullOrEmpty(term)) {
			return;
		}

		UserManager um = Gitop.getInstance(UserManager.class);
		int first = page * 25;
		Criterion criterion = Restrictions.or(
				Restrictions.ilike("name", term, MatchMode.START),
				Restrictions.ilike("displayName", term, MatchMode.START));
		List<User> users = um.query(new Criterion[] {criterion}, new Order[0], first, 25);

		response.addAll(users);
	}

	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
				.key("name").value(choice.getName())
				.key("displayName").value(choice.getDisplayName())
				.key("email").value(choice.getEmail())
				.key("avatar").value(getAvatarUrl(choice));
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		UserManager um = Gitop.getInstance(UserManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			users.add(um.load(id));
		}

		return users;
	}

	private String getAvatarUrl(User user) {
		String path = user.getAvatarUrl();
		if (GitopWebApp.get().isGravatarEnabled() && Strings.isNullOrEmpty(path)) {
			return Gravatar.getURL(user.getEmail());
		} else {
			CharSequence url = RequestCycle.get().urlFor(
					new AvatarImageResourceReference(),
					WicketUtils.newPageParams(
							"id",String.valueOf(user.getId()), 
							"type", "user"));

			return url.toString() + "?antiCache=" + System.currentTimeMillis();
		}
	}
}