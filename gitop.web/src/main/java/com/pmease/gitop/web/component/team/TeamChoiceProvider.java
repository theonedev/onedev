package com.pmease.gitop.web.component.team;

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
import com.pmease.gitop.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class TeamChoiceProvider extends ChoiceProvider<Team> {

	private static final long serialVersionUID = 1L;

	IModel<EntityCriteria<Team>> criteria;

	public TeamChoiceProvider(IModel<EntityCriteria<Team>> criteria) {
		this.criteria = criteria;
	}

	@Override
	public void query(String term, int page, Response<Team> response) {
		EntityCriteria<Team> crit = criteria == null ? null : criteria.getObject();
		if (crit == null) {
			crit = EntityCriteria.of(Team.class);
		}

		crit.add(Restrictions.ilike("name", term, MatchMode.START));
		crit.addOrder(Order.asc("name"));
		int first = page * 10;

		List<Team> teams = (List<Team>) Gitop.getInstance(Dao.class).query(crit, first, 10);
		
		response.addAll(teams);
	}

	@Override
	public void toJson(Team choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
				.key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()))
				.key("permission").value(StringEscapeUtils.escapeHtml4(choice.getAuthorizedOperation().toString()));
	}

	@Override
	public Collection<Team> toChoices(Collection<String> ids) {
		List<Team> teams = Lists.newArrayList();
		Dao dao = Gitop.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			teams.add(dao.load(Team.class, id));
		}

		return teams;
	}

	@Override
	public void detach() {
		super.detach();

		if (criteria != null) {
			criteria.detach();
		}
	}
}