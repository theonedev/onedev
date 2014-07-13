package com.pmease.gitop.web.component.repository;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class RepositoryChoiceProvider extends ChoiceProvider<Repository> {

	private static final int PAGE_SIZE = 25;
	
	private IModel<User> userModel;
	
	public RepositoryChoiceProvider(IModel<User> userModel) {
		this.userModel = userModel;
	}
	
	@Override
	public void query(String term, int page, Response<Repository> response) {
		Dao dao = Gitop.getInstance(Dao.class);
		int first = page * PAGE_SIZE;
		List<Repository> repositories = dao.query(EntityCriteria.of(Repository.class)
						.add(Restrictions.eq("owner", getUser()))
						.add(Restrictions.like("name", term, MatchMode.START).ignoreCase())
						.addOrder(Order.asc("name")), first, PAGE_SIZE);
		
		response.addAll(repositories);
	}

	@Override
	public void toJson(Repository choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
			  .key("owner").value(StringEscapeUtils.escapeHtml4(choice.getOwner().getName()))
			  .key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}

	@Override
	public Collection<Repository> toChoices(Collection<String> ids) {
		List<Repository> list = Lists.newArrayList();
		Dao dao = Gitop.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(dao.load(Repository.class, id));
		}
		
		return list;
	}

	private User getUser() {
		return userModel.getObject();
	}
	
	@Override
	public void detach() {
		if (userModel != null) {
			userModel.detach();
		}
		
		super.detach();
	}
}
