package com.gitplex.web.page.depot.setting.integrationpolicy;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.support.IntegrationPolicy;
import com.gitplex.core.manager.DepotManager;
import com.gitplex.web.page.depot.setting.DepotSettingPage;
import com.gitplex.commons.wicket.behavior.sortable.SortBehavior;
import com.gitplex.commons.wicket.behavior.sortable.SortPosition;

@SuppressWarnings("serial")
public class IntegrationPolicyPage extends DepotSettingPage {

	private WebMarkupContainer container;
	
	public IntegrationPolicyPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("integrationPolicySetting");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<IntegrationPolicy>("policies", new AbstractReadOnlyModel<List<IntegrationPolicy>>() {

			@Override
			public List<IntegrationPolicy> getObject() {
				return getDepot().getIntegrationPolicies();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<IntegrationPolicy> item) {
				item.add(new IntegrationPolicyPanel("policy", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						getDepot().getIntegrationPolicies().remove(item.getIndex());
						GitPlex.getInstance(DepotManager.class).save(getDepot(), null, null);
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, IntegrationPolicy policy) {
						getDepot().getIntegrationPolicies().set(item.getIndex(), policy);
						GitPlex.getInstance(DepotManager.class).save(getDepot(), null, null);
					}
					
				});
			}
			
		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<IntegrationPolicy> policies = getDepot().getIntegrationPolicies();
				IntegrationPolicy policy = policies.get(from.getItemIndex());
				policies.set(from.getItemIndex(), policies.set(to.getItemIndex(), policy));
				GitPlex.getInstance(DepotManager.class).save(getDepot(), null, null);
				
				target.add(container);
			}
			
		}.handle(".drag-handle").items("li.policy"));
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newPolicy", "addNewLinkFrag", this); 
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("newPolicy", "editNewFrag", getPage());
				fragment.setOutputMarkupId(true);
				fragment.add(new IntegrationPolicyEditor("editor", new IntegrationPolicy()) {

					@Override
					protected void onSave(AjaxRequestTarget target, IntegrationPolicy policy) {
						getDepot().getIntegrationPolicies().add(policy);
						GitPlex.getInstance(DepotManager.class).save(getDepot(), null, null);
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
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(IntegrationPolicyPage.class, IntegrationPolicyPage.paramsOf(depot));
	}
	
}
