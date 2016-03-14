package com.pmease.gitplex.web.page.organization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class NonMemberChoiceProvider extends AccountChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Account> organizationModel;
	
	public NonMemberChoiceProvider(IModel<Account> organizationModel) {
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
		for (Membership membership: organizationModel.getObject().getUserMemberships()) 
			members.add(membership.getUser());
		
		List<Account> nonMembers = new ArrayList<>();
		EntityCriteria<Account> criteria = accountManager.newCriteria(); 
		for (Account user: accountManager.query(criteria.add(criterion).addOrder(Order.asc("name")), 0, 0)) {
			if (!members.contains(user))
				nonMembers.add(user);
		}

		int from = page * Constants.DEFAULT_SELECT2_PAGE_SIZE;
		int to = from + Constants.DEFAULT_SELECT2_PAGE_SIZE;
		
		if (to > nonMembers.size()) {
			to = nonMembers.size();
		} 
		if (from > to) {
			from = to;
		}
		response.addAll(nonMembers.subList(from, to));
		response.setHasMore(to<nonMembers.size());
	}

	@Override
	public void detach() {
		organizationModel.detach();
		super.detach();
	}

}