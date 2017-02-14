package com.gitplex.server.web.component.teamchoice;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.wicket.component.select2.Response;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Team;
import com.gitplex.server.core.manager.TeamManager;
import com.gitplex.server.web.Constants;

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
		List<Team> teams = teamManager.findRange(criteria, first, Constants.DEFAULT_PAGE_SIZE + 1);

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