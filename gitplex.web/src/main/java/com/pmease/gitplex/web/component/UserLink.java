package com.pmease.gitplex.web.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;

@SuppressWarnings("serial")
public class UserLink extends BookmarkablePageLink<Void> {

	private final PageParameters params;
	
	private final String name;
	
	public UserLink(String id, User user) {
		super(id, AccountDepotsPage.class);

		params = AccountPage.paramsOf(user);
		name = user.getDisplayName();
	}
	
	public UserLink(String id, PersonIdent person) {
		super(id, AccountDepotsPage.class);
		
		User user = GitPlex.getInstance(UserManager.class).findByPerson(person);
		if (user != null) { 
			params = AccountPage.paramsOf(user);
			name = user.getDisplayName();
		} else {
			params = new PageParameters();
			name = person.getName();
		}
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(name);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (params.isEmpty()) {
			setEnabled(false);
			setBeforeDisabledLink("");
			setAfterDisabledLink("");
		}
	}

}
