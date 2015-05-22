package com.pmease.gitplex.web.page.repository.setting.integrationpolicy;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.sortable.SortBehavior;
import com.pmease.commons.wicket.behavior.sortable.SortPosition;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.IntegrationPolicy;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;
import com.pmease.gitplex.web.page.repository.setting.gatekeeper.GateKeeperPage;

@SuppressWarnings("serial")
public class IntegrationPolicyPage extends RepoSettingPage {

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
				return getRepository().getIntegrationPolicies();
			}
		}) {

			@Override
			protected void populateItem(final ListItem<IntegrationPolicy> item) {
				item.add(new IntegrationPolicyPanel("policy", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						getRepository().getIntegrationPolicies().remove(item.getIndex());
						GitPlex.getInstance(RepositoryManager.class).save(getRepository());
						target.add(container);
					}

					@Override
					protected void onSave(AjaxRequestTarget target, IntegrationPolicy policy) {
						getRepository().getIntegrationPolicies().set(item.getIndex(), policy);
						GitPlex.getInstance(RepositoryManager.class).save(getRepository());
					}
					
				});
			}
			
		});
		
		container.add(new SortBehavior() {
			
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<IntegrationPolicy> policies = getRepository().getIntegrationPolicies();
				IntegrationPolicy policy = policies.get(from.getItemIndex());
				policies.set(from.getItemIndex(), policies.set(to.getItemIndex(), policy));
				GitPlex.getInstance(RepositoryManager.class).save(getRepository());
				
				target.add(container);
			}
			
		}.handle(".handle").items("li"));
		
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
						getRepository().getIntegrationPolicies().add(policy);
						GitPlex.getInstance(RepositoryManager.class).save(getRepository());
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(GateKeeperPage.class, "integration-policy.css")));
	}
	
	@Override
	protected String getPageTitle() {
		return "Integration Policies - " + getRepository();
	}

}
