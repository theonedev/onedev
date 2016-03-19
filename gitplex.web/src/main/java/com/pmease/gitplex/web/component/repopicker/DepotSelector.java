package com.pmease.gitplex.web.component.repopicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;

@SuppressWarnings("serial")
public abstract class DepotSelector extends Panel {

	private final IModel<List<Depot>> depotsModel;
	
	private final Long currentDepotId;
	
	private String accountSearch = "";
	
	private String depotSearch = "";
	
	public DepotSelector(String id, IModel<List<Depot>> depotsModel, Long currentDepotId) {
		super(id);
		
		this.depotsModel = depotsModel;
		this.currentDepotId = currentDepotId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer dataContainer = new WebMarkupContainer("data") {

			@Override
			protected void onDetach() {
				depotsModel.detach();
				
				super.onDetach();
			}
			
		};
		dataContainer.setOutputMarkupId(true);
		add(dataContainer);
		
		TextField<String> accountSearchField = new TextField<String>("searchAccount", Model.of(""));
		add(accountSearchField);
		accountSearchField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				accountSearch = accountSearchField.getInput();
				if (accountSearch != null)
					accountSearch = accountSearch.trim().toLowerCase();
				else
					accountSearch = "";
				target.add(dataContainer);
			}
			
		});
		accountSearchField.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(Dao.class).load(Depot.class, id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.depotSelector.init('%s', %s)", 
						accountSearchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		TextField<String> depotSearchField = new TextField<String>("searchDepot", Model.of(""));
		add(depotSearchField);
		depotSearchField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				depotSearch = depotSearchField.getInput();
				if (depotSearch != null)
					depotSearch = depotSearch.trim().toLowerCase();
				else
					depotSearch = "";
				target.add(dataContainer);
			}
			
		});
		depotSearchField.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(Dao.class).load(Depot.class, id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.depotSelector.init('%s', %s)", 
						depotSearchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		dataContainer.add(new ListView<Account>("accounts", new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				List<Account> users = GitPlex.getInstance(Dao.class).allOf(Account.class);

				for (Iterator<Account> it = users.iterator(); it.hasNext();) {
					Account account = it.next();
					if (!account.getName().toLowerCase().contains(accountSearch)) {
						it.remove();
					} else {
						int depotCount = 0;
						for (Depot depot: depotsModel.getObject()) {
							if (depot.getName().contains(depotSearch) && depot.getAccount().equals(account))
								depotCount++;
						}
						if (depotCount == 0)
							it.remove();
					}
				}
				Collections.sort(users, new Comparator<Account>() {

					@Override
					public int compare(Account user1, Account user2) {
						return user1.getName().compareTo(user2.getName());
					}
					
				});
				return users;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Account> userItem) {
				userItem.add(new Avatar("avatar", userItem.getModelObject(), null));
				userItem.add(new Label("name", userItem.getModelObject().getName()));
				
				userItem.add(new ListView<Depot>("depots", new LoadableDetachableModel<List<Depot>>() {

					@Override
					protected List<Depot> load() {
						List<Depot> depots = new ArrayList<>();
						for (Depot depot: depotsModel.getObject()) {
							if (depot.getName().contains(depotSearch) && depot.getAccount().equals(userItem.getModelObject()))
								depots.add(depot);
						}
						Collections.sort(depots, new Comparator<Depot>() {

							@Override
							public int compare(Depot depot1, Depot depot2) {
								return depot1.getName().compareTo(depot2.getName());
							}
							
						});
						return depots;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Depot> depotItem) {
						Depot depot = depotItem.getModelObject();
						depotItem.add(new TextField<Long>("id", Model.of(depot.getId())));
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, depotItem.getModelObject());
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								
								HistoryState state = new HistoryState();
								PageParameters params = DepotFilePage.paramsOf(depotItem.getModelObject(), state);
								tag.put("href", urlFor(DepotFilePage.class, params));
							}
							
						};
						link.add(new Label("label", depot.getName()));
						if (depot.getId().equals(currentDepotId)) 
							link.add(AttributeAppender.append("class", " current"));
						depotItem.add(link);
						
						if (depotItem.getIndex() == 0 && userItem.getIndex() == 0)
							depotItem.add(AttributeAppender.append("class", " active"));
					}
					
				});
			}
			
		});
	}

	@Override
	protected void onDetach() {
		depotsModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DepotSelector.class, "depot-selector.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DepotSelector.class, "depot-selector.css")));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Depot depot);
}
