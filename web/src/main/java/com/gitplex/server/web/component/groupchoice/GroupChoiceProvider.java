package com.gitplex.server.web.component.groupchoice;

import java.util.List;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<Group> response) {
		GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
		int first = page * WebConstants.PAGE_SIZE;
		EntityCriteria<Group> criteria = groupManager.newCriteria();
		criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE));
		List<Group> groups = groupManager.findRange(criteria, first, WebConstants.PAGE_SIZE + 1);

		if (groups.size() <= WebConstants.PAGE_SIZE) {
			response.addAll(groups);
		} else {
			response.addAll(groups.subList(0, WebConstants.PAGE_SIZE));
			response.setHasMore(true);
		}
	}

}