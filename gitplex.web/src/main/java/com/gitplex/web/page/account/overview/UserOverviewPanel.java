package com.gitplex.web.page.account.overview;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.core.entity.Account;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.web.component.avatar.AvatarLink;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class UserOverviewPanel extends GenericPanel<Account> {

	public UserOverviewPanel(String id, IModel<Account> model) {
		super(id, model);
	}

	private Account getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = getUser();
		add(new Label("title", user.getDisplayName()));
		add(new Label("loginName", user.getName()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(user.getFullName()!=null);				
			}
			
		});
		add(new Label("email", user.getEmail())
				.add(AttributeAppender.append("href", "mailto:" + user.getEmail())));
		
		IModel<List<Account>> organizationsModel = new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> organizations = getUser().getOrganizations()
						.stream()
						// we do not want to disclose organization membership information
						.filter((membership)->membership.isAdmin() || SecurityUtils.canManage(getUser()))
						.map((membership)->membership.getOrganization())
						.collect(Collectors.toList());
				Collections.sort(organizations);
				return organizations;
			}
			
		};
		add(new WebMarkupContainer("organizations") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new ListView<Account>("organizations", organizationsModel) {

					@Override
					protected void populateItem(ListItem<Account> item) {
						item.add(new AvatarLink("organization", item.getModelObject(), new TooltipConfig()));
					}
					
				});
				
				add(new BookmarkablePageLink<Void>("addOrganization", 
						NewOrganizationPage.class, NewOrganizationPage.paramsOf(getUser())) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(user));				
					}
					
				});
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(user) || !organizationsModel.getObject().isEmpty());				
			}
			
		});
		
		add(new DepotListPanel("depots", getModel()));
	}

}
