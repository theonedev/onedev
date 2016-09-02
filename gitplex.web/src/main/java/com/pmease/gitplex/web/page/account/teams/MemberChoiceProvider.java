package com.pmease.gitplex.web.page.account.teams;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Response;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceProvider;

public class MemberChoiceProvider extends AccountChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Account> organizationModel;
	
	public MemberChoiceProvider(IModel<Account> organizationModel) {
		super(false);
		this.organizationModel = organizationModel;
	}
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		List<Account> members = new ArrayList<>();
		term = term.toLowerCase();
		for (OrganizationMembership membership: organizationModel.getObject().getOrganizationMembers()) { 
			Account user = membership.getUser();
			if (user.getName().toLowerCase().contains(term) 
					|| user.getFullName()!=null && user.getFullName().toLowerCase().contains(term)) {
				members.add(user);
			}
		}
		members.sort((user1, user2) -> user1.getName().compareTo(user2.getName()));
		
		int from = page * Constants.DEFAULT_PAGE_SIZE;
		int to = from + Constants.DEFAULT_PAGE_SIZE;
		
		if (to > members.size()) {
			to = members.size();
		} 
		if (from > to) {
			from = to;
		}
		response.addAll(members.subList(from, to));
		response.setHasMore(to<members.size());
	}

	@Override
	public void detach() {
		organizationModel.detach();
		super.detach();
	}

}