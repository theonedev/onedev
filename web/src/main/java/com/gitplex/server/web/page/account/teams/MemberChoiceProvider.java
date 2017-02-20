package com.gitplex.server.web.page.account.teams;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.OrganizationMembership;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.accountchoice.AccountChoiceProvider;
import com.gitplex.server.web.component.select2.Response;

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
		
		int from = page * WebConstants.DEFAULT_PAGE_SIZE;
		int to = from + WebConstants.DEFAULT_PAGE_SIZE;
		
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