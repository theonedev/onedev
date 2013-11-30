package com.pmease.gitop.web.component.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class TeamChoiceProvider extends ChoiceProvider<Team> {

	private static final long serialVersionUID = 1L;

	IModel<DetachedCriteria> criteria;

	public TeamChoiceProvider(IModel<DetachedCriteria> criteria) {
		this.criteria = criteria;
	}

	@Override
	public void query(String term, int page, Response<Team> response) {
		DetachedCriteria crit = criteria == null ? null : criteria.getObject();
		if (crit == null) {
			crit = DetachedCriteria.forClass(Team.class);
		}

		crit.add(Restrictions.ilike("name", term, MatchMode.START));
		crit.addOrder(Order.asc("name"));
		int first = page * 10;
		@SuppressWarnings("unchecked")
		List<Team> teams = (List<Team>) Gitop.getInstance(GeneralDao.class).query(crit, first, 10);
		
		response.addAll(teams);
	}

	@Override
	public void toJson(Team choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
				.key("name").value(choice.getName())
				.key("permission").value(choice.getAuthorizedOperation());
	}

	@Override
	public Collection<Team> toChoices(Collection<String> ids) {
		List<Team> teams = Lists.newArrayList();
		TeamManager tm = Gitop.getInstance(TeamManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			teams.add(tm.get(id));
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