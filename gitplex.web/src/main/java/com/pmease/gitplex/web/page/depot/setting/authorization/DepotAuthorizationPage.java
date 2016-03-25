package com.pmease.gitplex.web.page.depot.setting.authorization;

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
import com.pmease.gitplex.web.page.depot.setting.DepotSettingPage;

@SuppressWarnings("serial")
public abstract class DepotAuthorizationPage extends DepotSettingPage {

	public DepotAuthorizationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Teams"), DepotTeamListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, DepotTeamListPage.class, 
								DepotTeamListPage.paramsOf(depotModel.getObject()));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Collaborators"), DepotCollaboratorListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, DepotCollaboratorListPage.class, 
								DepotCollaboratorListPage.paramsOf(depotModel.getObject()));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Effective Privileges"), DepotEffectivePrivilegePage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, DepotEffectivePrivilegePage.class, 
								DepotEffectivePrivilegePage.paramsOf(depotModel.getObject()));
					}
					
				};
			}
			
		});
		add(new Tabbable("authorizationTabs", tabs).setVisible(getAccount().isOrganization()));
	}

}
