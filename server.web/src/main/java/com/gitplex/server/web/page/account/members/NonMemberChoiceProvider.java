package com.gitplex.server.web.page.account.members;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.wicket.component.select2.Response;
import com.gitplex.commons.wicket.component.select2.ResponseFiller;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.OrganizationMembership;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.web.Constants;
import com.gitplex.server.web.component.accountchoice.AccountChoiceProvider;

public class NonMemberChoiceProvider extends AccountChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Account> organizationModel;
	
	public NonMemberChoiceProvider(IModel<Account> organizationModel) {
		super(false);
		this.organizationModel = organizationModel;
	}
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
		Criterion criterion = Restrictions.and(
				Restrictions.or(
						Restrictions.ilike("name", term, MatchMode.ANYWHERE), 
						Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)), 
				Restrictions.eq("organization", false));
		
		Set<Account> members = new HashSet<>();
		for (OrganizationMembership membership: organizationModel.getObject().getOrganizationMembers()) 
			members.add(membership.getUser());
		
		List<Account> nonMembers = new ArrayList<>();
		EntityCriteria<Account> criteria = accountManager.newCriteria(); 
		for (Account user: accountManager.findRange(criteria.add(criterion).addOrder(Order.asc("name")), 0, 0)) {
			if (!members.contains(user))
				nonMembers.add(user);
		}

		new ResponseFiller<Account>(response).fill(nonMembers, page, Constants.DEFAULT_PAGE_SIZE);
	}

	@Override
	public void detach() {
		organizationModel.detach();
		super.detach();
	}

}