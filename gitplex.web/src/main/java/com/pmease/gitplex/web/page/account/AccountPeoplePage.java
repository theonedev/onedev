package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.collaborators.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorPage;
import com.pmease.gitplex.web.page.account.members.MemberListPage;
import com.pmease.gitplex.web.page.account.members.MemberPage;

@SuppressWarnings("serial")
public abstract class AccountPeoplePage extends AccountLayoutPage {

	public AccountPeoplePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Members"), MemberListPage.class, MemberPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, MemberListPage.class, 
								MemberListPage.paramsOf(getAccount()));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Outside Collaborators"), 
				AccountCollaboratorListPage.class, CollaboratorPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, AccountCollaboratorListPage.class, 
								AccountCollaboratorListPage.paramsOf(getAccount()));
					}
					
				};
			}
			
		});
		boolean visible = getAccount().isOrganization() && SecurityUtils.canManage(getAccount());
		add(new Tabbable("peopleTabs", tabs).setVisible(visible));
	}

}
