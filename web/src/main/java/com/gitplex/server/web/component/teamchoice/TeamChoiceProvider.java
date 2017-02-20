package com.gitplex.server.web.component.teamchoice;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Team;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;

public class TeamChoiceProvider extends AbstractTeamChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<Account> organizationModel;
		
	public TeamChoiceProvider(IModel<Account> organizationModel) {
		this.organizationModel = organizationModel;
	}

	@Override
	public void query(String term, int page, Response<Team> response) {
		TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
		int first = page * WebConstants.DEFAULT_PAGE_SIZE;
		EntityCriteria<Team> criteria = teamManager.newCriteria();
		criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE))
				.add(Restrictions.eq("organization", organizationModel.getObject()));
		List<Team> teams = teamManager.findRange(criteria, first, WebConstants.DEFAULT_PAGE_SIZE + 1);

		if (teams.size() <= WebConstants.DEFAULT_PAGE_SIZE) {
			response.addAll(teams);
		} else {
			response.addAll(teams.subList(0, WebConstants.DEFAULT_PAGE_SIZE));
			response.setHasMore(true);
		}
	}

	@Override
	public void detach() {
		super.detach();

		organizationModel.detach();
	}
}