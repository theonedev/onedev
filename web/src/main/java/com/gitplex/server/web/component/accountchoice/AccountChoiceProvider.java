package com.gitplex.server.web.component.accountchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.util.WicketUtils;

public class AccountChoiceProvider extends AbstractAccountChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final AccountChoice.Type type;
	
	public AccountChoiceProvider(AccountChoice.Type type) {
		this.type = type;
	}
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		if (type == AccountChoice.Type.ORGANIZATION || type == AccountChoice.Type.USER) {
			Dao dao = GitPlex.getInstance(Dao.class);
			int first = page * WebConstants.DEFAULT_PAGE_SIZE;
			Criterion criterion = Restrictions.and(Restrictions.or(
					Restrictions.ilike("name", term, MatchMode.ANYWHERE),
					Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)));
			EntityCriteria<Account> criteria = EntityCriteria.of(Account.class);
			criteria.add(criterion);
			criteria.add(Restrictions.eq("organization", type == AccountChoice.Type.ORGANIZATION));
			criteria.addOrder(Order.asc("name"));
			List<Account> accounts = dao.findRange(criteria, first, WebConstants.DEFAULT_PAGE_SIZE + 1);

			if (accounts.size() <= WebConstants.DEFAULT_PAGE_SIZE) {
				response.addAll(accounts);
			} else {
				response.addAll(accounts.subList(0, WebConstants.DEFAULT_PAGE_SIZE));
				response.setHasMore(true);
			}
		} else {
			Depot depot = ((DepotPage) WicketUtils.getPage()).getDepot();
			List<Account> choices;
			if (type == AccountChoice.Type.DEPOT_READER)
				choices = new ArrayList<Account>(SecurityUtils.findUsersCan(depot, DepotPrivilege.READ));
			else if (type == AccountChoice.Type.DEPOT_WRITER)
				choices = new ArrayList<Account>(SecurityUtils.findUsersCan(depot, DepotPrivilege.WRITE));
			else
				choices = new ArrayList<Account>(SecurityUtils.findUsersCan(depot, DepotPrivilege.ADMIN));
				
			choices.sort(Comparator.comparing(Account::getName));
			
			new ResponseFiller<Account>(response).fill(choices, page, WebConstants.DEFAULT_PAGE_SIZE);
		}
	}

}