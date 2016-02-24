package com.pmease.gitplex.web.page.account.depots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModal;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class AccountDepotsPage extends AccountLayoutPage {

	private PageableListView<Depot> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private String searchInput = "";
	
	public AccountDepotsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Repositories - " + getAccount();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final TextField<String> searchField;
		
		add(searchField = new ClearableTextField<String>("searchRepos", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(depotsContainer);
				target.add(pagingNavigator);
			}
			
		});
		
		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}

			@Override
			public void onClick() {
				Depot depot = new Depot();
				depot.setOwner(getAccount());
				setResponsePage(new NewAccountDepotPage(depot));
			}
			
		});
		
		depotsContainer = new WebMarkupContainer("reposContainer");
		depotsContainer.setOutputMarkupId(true);
		add(depotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<Depot>("repositories", new LoadableDetachableModel<List<Depot>>() {

			@Override
			protected List<Depot> load() {
				List<Depot> repositories = new ArrayList<>();
				
				searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (Depot repo: getAccount().getDepots()) {
					if (repo.getName().toLowerCase().contains(searchInput) && SecurityUtils.canPull(repo))
						repositories.add(repo);
				}
				
				Collections.sort(repositories, new Comparator<Depot>() {

					@Override
					public int compare(Depot repository1, Depot repository2) {
						return repository1.getName().compareTo(repository2.getName());
					}
					
				});
				return repositories;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Depot> item) {
				Depot depot = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("repoLink", DepotFilePage.class, DepotFilePage.paramsOf(depot)); 
				link.add(new Label("repoName", depot.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("description", depot.getDescription()));
				
				item.add(new Link<Void>("setting") {

					@Override
					public void onClick() {
						PageParameters params = GeneralSettingPage.paramsOf(item.getModelObject());
						setResponsePage(GeneralSettingPage.class, params);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}
					
				});
				
				final Long repositoryId = depot.getId();
				item.add(new AjaxLink<Void>("deleteRepo") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteRepoModal(target) {

							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								setResponsePage(AccountDepotsPage.this);
							}

							@Override
							protected Depot getDepot() {
								return GitPlex.getInstance(Dao.class).load(Depot.class, repositoryId);
							}
							
						};
					}
					
				});
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("reposPageNav", depotsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AccountDepotsPage.class, "account-depots.css")));
	}

}
