package com.gitplex.server.web.component.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.page.project.ProjectPage;
import com.gitplex.server.web.util.WicketUtils;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final UserChoice.Type type;
	
	public UserChoiceProvider(UserChoice.Type type) {
		this.type = type;
	}
	
	@Override
	public void query(String term, int page, Response<User> response) {
		if (type == UserChoice.Type.ALL) {
			Dao dao = GitPlex.getInstance(Dao.class);
			int first = page * WebConstants.PAGE_SIZE;
			Criterion criterion = Restrictions.and(Restrictions.or(
					Restrictions.ilike("name", term, MatchMode.ANYWHERE),
					Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)));
			EntityCriteria<User> criteria = EntityCriteria.of(User.class);
			criteria.add(criterion);
			criteria.addOrder(Order.asc("name"));
			List<User> users = dao.findRange(criteria, first, WebConstants.PAGE_SIZE + 1);

			if (users.size() <= WebConstants.PAGE_SIZE) {
				response.addAll(users);
			} else {
				response.addAll(users.subList(0, WebConstants.PAGE_SIZE));
				response.setHasMore(true);
			}
		} else {
			Project project = ((ProjectPage) WicketUtils.getPage()).getProject();
			List<User> choices;
			if (type == UserChoice.Type.DEPOT_READER)
				choices = new ArrayList<User>(SecurityUtils.findUsersCan(project, ProjectPrivilege.READ));
			else if (type == UserChoice.Type.DEPOT_WRITER)
				choices = new ArrayList<User>(SecurityUtils.findUsersCan(project, ProjectPrivilege.WRITE));
			else
				choices = new ArrayList<User>(SecurityUtils.findUsersCan(project, ProjectPrivilege.ADMIN));
				
			choices.sort(Comparator.comparing(User::getName));
			
			new ResponseFiller<User>(response).fill(choices, page, WebConstants.PAGE_SIZE);
		}
	}

}