package com.pmease.gitplex.web.page.account.people;

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
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.organization.member.MemberListPage;

@SuppressWarnings("serial")
public abstract class AccountPeoplePage extends AccountLayoutPage {

	public AccountPeoplePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Members"), MemberListPage.class) {

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
		add(new Tabbable("peopleTabs", tabs).setVisible(getAccount().isOrganization()));
	}

}
