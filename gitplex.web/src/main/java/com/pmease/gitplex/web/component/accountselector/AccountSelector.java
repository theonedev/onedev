package com.pmease.gitplex.web.component.accountselector;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public abstract class AccountSelector extends Panel {

	private final IModel<List<Account>> accountsModel;
	
	private final Long currentAccountId;
	
	public AccountSelector(String id, IModel<List<Account>> accountsModel, Long currentAccountId) {
		super(id);
		
		this.accountsModel = accountsModel;
		this.currentAccountId = currentAccountId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer accountsContainer = new WebMarkupContainer("accounts");
		accountsContainer.setOutputMarkupId(true);
		add(accountsContainer);
		
		TextField<String> searchField = new TextField<String>("search", Model.of(""));
		add(searchField);
		searchField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				target.add(accountsContainer);
			}
			
		});
		searchField.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(AccountManager.class).load(id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.accountSelector.init('%s', %s)", 
						searchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		accountsContainer.add(new ListView<Account>("accounts", new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> accounts = new ArrayList<>();
				for (Account account: accountsModel.getObject()) {
					if (account.matches(searchField.getInput())) {
						accounts.add(account);
					}
				}
				return accounts;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Account> item) {
				Account account = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, item.getModelObject());
					}
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						PageParameters params = AccountOverviewPage.paramsOf(item.getModelObject());
						tag.put("href", urlFor(AccountOverviewPage.class, params));
					}
					
				};
				link.add(new Avatar("avatar", item.getModelObject(), null));
				link.add(new Label("name", item.getModelObject().getName()));
				if (account.getId().equals(currentAccountId)) 
					link.add(AttributeAppender.append("class", " current"));
				item.add(link);
				
				if (item.getIndex() == 0)
					item.add(AttributeAppender.append("class", "active"));
				item.add(AttributeAppender.append("data-id", account.getId()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		accountsModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(AccountSelector.class, "account-selector.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AccountSelector.class, "account-selector.css")));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Account account);
}
