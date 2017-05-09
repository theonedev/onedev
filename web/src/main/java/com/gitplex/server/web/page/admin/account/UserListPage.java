package com.gitplex.server.web.page.admin.account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.page.account.AccountPage;
import com.gitplex.server.web.page.account.setting.ProfileEditPage;
import com.gitplex.server.web.page.admin.AdministrationPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private DataTable<Account, Void> accountsTable;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer noAccountsContainer;
	
	private String searchInput;
	
	private EntityCriteria<Account> getCriteria() {
		EntityCriteria<Account> criteria = EntityCriteria.of(Account.class);
		if (searchInput != null) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE), 
					Restrictions.ilike("fullName", searchInput, MatchMode.ANYWHERE)));
		}
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("searchAccounts", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(accountsTable);
				target.add(pagingNavigator);
				target.add(noAccountsContainer);
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
		
		List<IColumn<Account, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Account, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Account>> cellItem, String componentId,
					IModel<Account> rowModel) {
				Account account = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", UserListPage.this);
				fragment.add(new AvatarLink("avatarLink", account));
				fragment.add(new AccountLink("nameLink", account));
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Account, Void>(Model.of("Email")) {

			@Override
			public void populateItem(Item<ICellPopulator<Account>> cellItem, String componentId,
					IModel<Account> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmail()));
			}
		});
		
		columns.add(new AbstractColumn<Account, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Account>> cellItem, String componentId,
					IModel<Account> rowModel) {
				Account account = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "actionFrag", UserListPage.this);
				fragment.add(new Link<Void>("setting") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(rowModel.getObject());
						setResponsePage(ProfileEditPage.class, params);
					}

				});
				
				Long accountId = account.getId();
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Account account = GitPlex.getInstance(AccountManager.class).load(accountId);
						if (!account.getDepots().isEmpty()) {
							target.appendJavaScript("alert('Please delete or transfer repositories under this account first');");
						} else {
							new ConfirmDeleteAccountModal(target) {
								
								@Override
								protected void onDeleted(AjaxRequestTarget target) {
									target.add(accountsTable);
									target.add(pagingNavigator);
									target.add(noAccountsContainer);
								}
								
								@Override
								protected Account getAccount() {
									return GitPlex.getInstance(AccountManager.class).load(accountId);
								}
							};
						}
					}

				});
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Account, Void> dataProvider = new SortableDataProvider<Account, Void>() {

			@Override
			public Iterator<? extends Account> iterator(long first, long count) {
				EntityCriteria<Account> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return GitPlex.getInstance(AccountManager.class).findRange(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return GitPlex.getInstance(AccountManager.class).count(getCriteria());
			}

			@Override
			public IModel<Account> model(Account object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Account>() {

					@Override
					protected Account load() {
						return GitPlex.getInstance(AccountManager.class).load(id);
					}
					
				};
			}
		};
		
		add(accountsTable = new DataTable<Account, Void>("accounts", columns, dataProvider, 
				WebConstants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getItemCount() != 0);
			}
						
		});
		accountsTable.setOutputMarkupPlaceholderTag(true);
		
		add(pagingNavigator = new BootstrapAjaxPagingNavigator("accountsPageNav", accountsTable) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsTable.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		add(noAccountsContainer = new WebMarkupContainer("noAccounts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsTable.getItemCount() == 0);
			}
			
		});
		noAccountsContainer.setOutputMarkupPlaceholderTag(true);
	}

}
