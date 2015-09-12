package com.pmease.gitplex.web.component.reposelector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

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
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;
import com.pmease.gitplex.web.page.repository.file.HistoryState;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public abstract class RepositorySelector extends Panel {

	private final IModel<Collection<Repository>> reposModel;
	
	private final IModel<Repository> currentRepoModel;
	
	private String accountSearch = "";
	
	private String repoSearch = "";
	
	public RepositorySelector(String id, @Nullable IModel<Collection<Repository>> reposModel, 
			IModel<Repository> currentRepoModel) {
		super(id);
		
		if (reposModel != null) {
			this.reposModel = reposModel;
		} else {
			this.reposModel = new LoadableDetachableModel<Collection<Repository>>() {

				@Override
				protected Collection<Repository> load() {
					Collection<Repository> repositories = new ArrayList<>(); 
					for (Repository repo: GitPlex.getInstance(Dao.class).allOf(Repository.class)) {
						if (SecurityUtils.canPull(repo))
							repositories.add(repo);
					}
					return repositories;
				}
			
			};
		}
		this.currentRepoModel = currentRepoModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer dataContainer = new WebMarkupContainer("data") {

			@Override
			protected void onDetach() {
				reposModel.detach();
				
				super.onDetach();
			}
			
		};
		dataContainer.setOutputMarkupId(true);
		add(dataContainer);
		
		final TextField<String> accountSearchField = new TextField<String>("searchAccount", Model.of(""));
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
				onSelect(target, GitPlex.getInstance(Dao.class).load(Repository.class, id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.repositorySelector.init('%s', %s)", 
						accountSearchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		final TextField<String> repoSearchField = new TextField<String>("searchRepo", Model.of(""));
		add(repoSearchField);
		repoSearchField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				repoSearch = repoSearchField.getInput();
				if (repoSearch != null)
					repoSearch = repoSearch.trim().toLowerCase();
				else
					repoSearch = "";
				target.add(dataContainer);
			}
			
		});
		repoSearchField.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(Dao.class).load(Repository.class, id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.repositorySelector.init('%s', %s)", 
						repoSearchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		dataContainer.add(new ListView<User>("accounts", new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				List<User> users = GitPlex.getInstance(Dao.class).allOf(User.class);

				for (Iterator<User> it = users.iterator(); it.hasNext();) {
					User account = it.next();
					if (!account.getName().toLowerCase().contains(accountSearch)) {
						it.remove();
					} else {
						int repoCount = 0;
						for (Repository repo: reposModel.getObject()) {
							if (repo.getName().contains(repoSearch) && repo.getOwner().equals(account))
								repoCount++;
						}
						if (repoCount == 0)
							it.remove();
					}
				}
				Collections.sort(users, new Comparator<User>() {

					@Override
					public int compare(User user1, User user2) {
						return user1.getName().compareTo(user2.getName());
					}
					
				});
				return users;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<User> userItem) {
				userItem.add(new AvatarByUser("avatar", userItem.getModel(), false));
				userItem.add(new Label("name", userItem.getModelObject().getName()));
				
				userItem.add(new ListView<Repository>("repositories", new LoadableDetachableModel<List<Repository>>() {

					@Override
					protected List<Repository> load() {
						List<Repository> repositories = new ArrayList<>();
						for (Repository repo: reposModel.getObject()) {
							if (repo.getName().contains(repoSearch) && repo.getOwner().equals(userItem.getModelObject()))
								repositories.add(repo);
						}
						Collections.sort(repositories, new Comparator<Repository>() {

							@Override
							public int compare(Repository repo1, Repository repo2) {
								return repo1.getName().compareTo(repo2.getName());
							}
							
						});
						return repositories;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Repository> repoItem) {
						Repository repository = repoItem.getModelObject();
						repoItem.add(new TextField<Long>("id", Model.of(repository.getId())));
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, repoItem.getModelObject());
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								
								HistoryState state = new HistoryState();
								PageParameters params = RepoFilePage.paramsOf(repoItem.getModelObject(), state);
								tag.put("href", urlFor(RepoFilePage.class, params));
							}
							
						};
						link.add(new Label("label", repository.getName()));
						if (repository.equals(currentRepoModel.getObject())) 
							link.add(AttributeAppender.append("class", " current"));
						repoItem.add(link);
						
						if (repoItem.getIndex() == 0 && userItem.getIndex() == 0)
							repoItem.add(AttributeAppender.append("class", " active"));
					}
					
				});
			}
			
		});
	}

	@Override
	protected void onDetach() {
		reposModel.detach();
		currentRepoModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepositorySelector.class, "repository-selector.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepositorySelector.class, "repository-selector.css")));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Repository repository);
}
