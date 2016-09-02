package com.pmease.gitplex.web.component.accountchoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.component.select2.Response;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;

public class AdministrativeAccountChoiceProvider extends AbstractAccountChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<Account> response) {
		Account loginUser = Preconditions.checkNotNull(SecurityUtils.getAccount());
		int pageSize = Constants.DEFAULT_PAGE_SIZE;
		if (loginUser.isAdministrator()) {
			AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
			int first = page * pageSize;
			Criterion criterion = Restrictions.and(Restrictions.or(
					Restrictions.ilike("name", term, MatchMode.ANYWHERE),
					Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)));
			List<Account> accounts = accountManager.findRange(accountManager.newCriteria()
					.add(criterion).addOrder(Order.asc("name")), first, pageSize + 1);

			if (accounts.size() <= pageSize) {
				response.addAll(accounts);
			} else {
				response.addAll(accounts.subList(0, pageSize));
				response.setHasMore(true);
			}
		} else {
			List<Account> accounts = new ArrayList<>();
			if (loginUser.matches(term))
				accounts.add(loginUser);
			for (OrganizationMembership membership: loginUser.getOrganizations()) {
				if (membership.isAdmin() && membership.getOrganization().matches(term))
					accounts.add(membership.getOrganization());
			}

			Collections.sort(accounts);
			
			new ResponseFiller<Account>(response).fill(accounts, page, pageSize);
		}
	}

}
