package com.pmease.gitplex.web.component.user;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class UserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void query(String term, int page, Response<User> response) {
		Dao dao = GitPlex.getInstance(Dao.class);
		int first = page * Constants.DEFAULT_SELECT2_PAGE_SIZE;
		Criterion criterion = Restrictions.and(Restrictions.or(
				Restrictions.ilike("name", term, MatchMode.START),
				Restrictions.ilike("fullName", term, MatchMode.START)));
		List<User> users = dao.query(EntityCriteria.of(User.class)
				.add(criterion).addOrder(Order.asc("name")), first, Constants.DEFAULT_SELECT2_PAGE_SIZE + 1);

		if (users.size() <= Constants.DEFAULT_SELECT2_PAGE_SIZE) {
			response.addAll(users);
		} else {
			response.addAll(users.subList(0, Constants.DEFAULT_SELECT2_PAGE_SIZE));
			response.setHasMore(true);
		}
	}

	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
		if (choice.getFullName() != null)
			writer.key("fullName").value(StringEscapeUtils.escapeHtml4(choice.getFullName()));
		writer.key("email").value(StringEscapeUtils.escapeHtml4(choice.getEmail()));
		String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			users.add(dao.load(User.class, id));
		}

		return users;
	}

}