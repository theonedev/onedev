package com.pmease.gitplex.web.page.admin.account;

import static com.pmease.gitplex.web.page.admin.account.TypeSelectionPanel.TYPE_ORGANIZATIOIN;
import static com.pmease.gitplex.web.page.admin.account.TypeSelectionPanel.TYPE_USER;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.admin.AdministrationPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class AccountListPage extends AdministrationPage {

	private String filterType;
	
	private PageableListView<Account> accountsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer accountsContainer; 
	
	private WebMarkupContainer noAccountsContainer;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("searchAccounts", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(accountsContainer);
				target.add(pagingNavigator);
				target.add(noAccountsContainer);
			}

		});
		
		WebMarkupContainer filterContainer = new WebMarkupContainer("filter");
		filterContainer.setOutputMarkupId(true);
		add(filterContainer);
		
		filterContainer.add(new DropdownLink("selection") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (filterType == null)
							return "Filter by account type";
						else 
							return filterType;
					}
					
				}));
			}

			@Override
			protected Component newContent(String id) {
				return new TypeSelectionPanel(id, filterType) {

					@Override
					protected void onSelectUser(AjaxRequestTarget target) {
						close();
						filterType = TYPE_USER;
						target.add(filterContainer);
						target.add(accountsContainer);
						target.add(pagingNavigator);
						target.add(noAccountsContainer);
					}

					@Override
					protected void onSelectOrganization(AjaxRequestTarget target) {
						close();
						filterType = TYPE_ORGANIZATIOIN;
						target.add(filterContainer);
						target.add(accountsContainer);
						target.add(pagingNavigator);
						target.add(noAccountsContainer);
					}
					
				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterType = null;
				target.add(filterContainer);
				target.add(accountsContainer);
				target.add(pagingNavigator);
				target.add(noAccountsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterType != null);
			}
			
		});

		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageSystem());
			}

			@Override
			public void onClick() {
				setResponsePage(NewUserPage.class);
			}
			
		});
		
		accountsContainer = new WebMarkupContainer("accounts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!accountsView.getModelObject().isEmpty());
			}
			
		};
		accountsContainer.setOutputMarkupPlaceholderTag(true);
		add(accountsContainer);
		
		accountsContainer.add(accountsView = new PageableListView<Account>("accounts", new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> accounts = new ArrayList<>();
				for (Account account: GitPlex.getInstance(AccountManager.class).all()) {
					if (account.matches(searchField.getInput())) {
						if (filterType == null 
								|| filterType.equals(TYPE_USER) && !account.isOrganization() 
								|| filterType.equals(TYPE_ORGANIZATIOIN) && account.isOrganization()) {
							accounts.add(account);
						}
					}
				}
				accounts.sort((account1, account2) -> account1.getName().compareTo(account2.getName()));
				return accounts;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account account = item.getModelObject();

				item.add(new AvatarLink("avatarLink", item.getModelObject(), null));
				item.add(new AccountLink("nameLink", item.getModelObject()));
				item.add(new Label("type", account.isOrganization()?TYPE_ORGANIZATIOIN:TYPE_USER));
				
				item.add(new Link<Void>("setting") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						setResponsePage(ProfileEditPage.class, params);
					}

				});
				
				Long accountId = account.getId();
				item.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteAccountModal(target) {
							
							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								target.add(accountsContainer);
								target.add(pagingNavigator);
								target.add(noAccountsContainer);
							}
							
							@Override
							protected Account getAccount() {
								return GitPlex.getInstance(AccountManager.class).load(accountId);
							}
						};
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						Account account = item.getModelObject();
						setVisible(SecurityUtils.canManage(account) && !account.equals(getLoginUser()));
					}

				});
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("accountsPageNav", accountsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		add(noAccountsContainer = new WebMarkupContainer("noAccounts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsView.getModelObject().isEmpty());
			}
			
		});
		noAccountsContainer.setOutputMarkupPlaceholderTag(true);
	}

}
