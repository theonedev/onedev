package com.pmease.gitplex.web.page.account.subscription;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.behavior.sortable.SortBehavior;
import com.pmease.commons.wicket.behavior.sortable.SortPosition;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.ChangeSubscription;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class ChangeSubscriptionPage extends AccountPage {

	private WebMarkupContainer container;
	
	public ChangeSubscriptionPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("changeSubscriptions");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<ChangeSubscription>("subscriptions", new AbstractReadOnlyModel<List<ChangeSubscription>>() {

			@Override
			public List<ChangeSubscription> getObject() {
				return getAccount().getChangeSubscriptions();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<ChangeSubscription> item) {
				item.add(new ChangeSubscriptionPanel("subscription", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						getAccount().getChangeSubscriptions().remove(item.getIndex());
						GitPlex.getInstance(UserManager.class).save(getAccount());
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, ChangeSubscription subscription) {
						getAccount().getChangeSubscriptions().set(item.getIndex(), subscription);
						GitPlex.getInstance(UserManager.class).save(getAccount());
					}
					
				});
			}
			
		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<ChangeSubscription> subscriptions = getAccount().getChangeSubscriptions();
				ChangeSubscription subscription = subscriptions.get(from.getItemIndex());
				subscriptions.set(from.getItemIndex(), subscriptions.set(to.getItemIndex(), subscription));
				GitPlex.getInstance(UserManager.class).save(getAccount());
				
				target.add(container);
			}
			
		}.handle(".handle").items("li"));
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newSubscription", "addNewLinkFrag", this); 
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newSubscription", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new ChangeSubscriptionEditor("editor", new ChangeSubscription()) {

					@Override
					protected void onSave(AjaxRequestTarget target, ChangeSubscription subscription) {
						getAccount().getChangeSubscriptions().add(subscription);
						GitPlex.getInstance(UserManager.class).save(getAccount());
						container.replace(newAddNewFrag());
						target.add(container);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component newAddNewFrag = newAddNewFrag();
						container.replace(newAddNewFrag);
						target.add(newAddNewFrag);
					}
					
				});
				container.replace(fragment);
				target.add(fragment);
			}
			
		});
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	@Override
	protected String getPageTitle() {
		return "Change Subscriptions - " + getAccount();
	}

	@Override
	protected boolean isPermitted() {
		return super.isPermitted() 
				&& SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount()));
	}

}
