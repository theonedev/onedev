package com.pmease.gitplex.web.component.accountchoice;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.assets.accountchoice.AbstractAccountChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class AccountChoiceProvider extends AbstractAccountChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		Dao dao = GitPlex.getInstance(Dao.class);
		int first = page * Constants.DEFAULT_PAGE_SIZE;
		Criterion criterion = Restrictions.and(Restrictions.or(
				Restrictions.ilike("name", term, MatchMode.ANYWHERE),
				Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)));
		List<Account> accounts = dao.query(EntityCriteria.of(Account.class)
				.add(criterion).addOrder(Order.asc("name")), first, Constants.DEFAULT_PAGE_SIZE + 1);

		if (accounts.size() <= Constants.DEFAULT_PAGE_SIZE) {
			response.addAll(accounts);
		} else {
			response.addAll(accounts.subList(0, Constants.DEFAULT_PAGE_SIZE));
			response.setHasMore(true);
		}
	}

}