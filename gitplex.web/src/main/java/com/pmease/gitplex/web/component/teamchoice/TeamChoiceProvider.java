package com.pmease.gitplex.web.component.teamchoice;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.Response;

public class TeamChoiceProvider extends AbstractTeamChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<Account> organizationModel;
		
	public TeamChoiceProvider(IModel<Account> organizationModel) {
		this.organizationModel = organizationModel;
	}

	@Override
	public void query(String term, int page, Response<Team> response) {
		TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
		int first = page * Constants.DEFAULT_PAGE_SIZE;
		EntityCriteria<Team> criteria = teamManager.newCriteria();
		criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE))
				.add(Restrictions.eq("organization", organizationModel.getObject()));
		List<Team> teams = teamManager.query(criteria, first, Constants.DEFAULT_PAGE_SIZE + 1);

		if (teams.size() <= Constants.DEFAULT_PAGE_SIZE) {
			response.addAll(teams);
		} else {
			response.addAll(teams.subList(0, Constants.DEFAULT_PAGE_SIZE));
			response.setHasMore(true);
		}
	}

	@Override
	public void detach() {
		super.detach();

		organizationModel.detach();
	}
}