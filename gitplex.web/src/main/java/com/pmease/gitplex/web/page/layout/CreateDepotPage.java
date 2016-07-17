package com.pmease.gitplex.web.page.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.accountchoice.AbstractAccountChoiceProvider;
import com.pmease.gitplex.web.component.accountchoice.AccountSingleChoice;
import com.pmease.gitplex.web.page.depot.overview.DepotOverviewPage;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class CreateDepotPage extends LayoutPage {

	private Long ownerId;
	
	public CreateDepotPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Depot depot = new Depot();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", depot);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Account owner = getOwner();
				depot.setAccount(owner);
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.find(owner, depot.getName());
				if (depotWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another repository in account '" + owner.getDisplayName() + "'");
				} else {
					depotManager.save(depot, null, null);
					Session.get().success("New repository created");
					setResponsePage(DepotOverviewPage.class, DepotOverviewPage.paramsOf(depot));
				}
			}
			
		};
		form.add(editor);
		
		IModel<Account> choiceModel = new IModel<Account>() {

			@Override
			public void detach() {
			}

			@Override
			public Account getObject() {
				return getOwner();
			}

			@Override
			public void setObject(Account object) {
				ownerId = object.getId();
			}
			
		};
		form.add(new AccountSingleChoice("owner", choiceModel, new AbstractAccountChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Account> response) {
				Account loginUser = getLoginUser();
				if (loginUser.isAdministrator()) {
					AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
					int first = page * Constants.DEFAULT_PAGE_SIZE;
					Criterion criterion = Restrictions.and(Restrictions.or(
							Restrictions.ilike("name", term, MatchMode.ANYWHERE),
							Restrictions.ilike("fullName", term, MatchMode.ANYWHERE)));
					List<Account> accounts = accountManager.findRange(accountManager.newCriteria()
							.add(criterion).addOrder(Order.asc("name")), first, Constants.DEFAULT_PAGE_SIZE + 1);

					if (accounts.size() <= Constants.DEFAULT_PAGE_SIZE) {
						response.addAll(accounts);
					} else {
						response.addAll(accounts.subList(0, Constants.DEFAULT_PAGE_SIZE));
						response.setHasMore(true);
					}
				} else {
					List<Account> accounts = new ArrayList<>();
					if (loginUser.matches(term))
						accounts.add(loginUser);
					for (OrganizationMembership membership: loginUser.getOrganizations()) {
						if (membership.isAdmin() && membership.getOrganization().matches(term))
							accounts.add(membership.getOrganization());
					}

					Collections.sort(accounts);
					
					new ResponseFiller<Account>(response).fill(accounts, page, Constants.DEFAULT_PAGE_SIZE);
				}
			}
			
		}).setRequired(true));
		
		add(form);
	}

	private Account getOwner() {
		if (ownerId == null)
			return getLoginUser();
		else
			return GitPlex.getInstance(AccountManager.class).load(ownerId);
	}
	
}
