package com.pmease.gitplex.web.component.accountselector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public abstract class OrganizationSelector extends GenericPanel<Account> {

	private final Long currentOrganizationId;

	private ListView<Account> organizationsView;
	
	public OrganizationSelector(String id, IModel<Account> accountModel, Long currentOrganizationId) {
		super(id, accountModel);
		this.currentOrganizationId = currentOrganizationId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer organizationsContainer = new WebMarkupContainer("organizations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!organizationsView.getModelObject().isEmpty());
			}
			
		};
		organizationsContainer.setOutputMarkupPlaceholderTag(true);
		add(organizationsContainer);
		
		WebMarkupContainer noorganizationsContainer = new WebMarkupContainer("noOrganizations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(organizationsView.getModelObject().isEmpty());
			}
			
		};
		noorganizationsContainer.setOutputMarkupPlaceholderTag(true);
		add(noorganizationsContainer);
		
		organizationsContainer.add(organizationsView = new ListView<Account>("organizations", 
				new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> organizations = getAccount().getOrganizations()
						.stream()
						.map(membership->membership.getOrganization())
						.collect(Collectors.toList());
				Collections.sort(organizations);
				return organizations;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account organization = item.getModelObject();
				AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, item.getModelObject());
					}
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						PageParameters params = AccountOverviewPage.paramsOf(item.getModelObject());
						tag.put("href", urlFor(AccountOverviewPage.class, params).toString());
					}
					
				};
				if (organization.getId().equals(currentOrganizationId)) 
					link.add(AttributeAppender.append("class", " current"));
				link.add(new Avatar("avatar", organization));
				link.add(new Label("name", organization.getDisplayName()));
				item.add(link);
				
				if (item.getIndex() == 0)
					item.add(AttributeAppender.append("class", "active"));
			}
			
		});
	}

	private Account getAccount() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(OrganizationSelector.class, "organization-selector.css")));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Account organization);
	
}
