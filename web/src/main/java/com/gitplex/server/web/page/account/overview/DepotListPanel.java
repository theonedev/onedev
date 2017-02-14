package com.gitplex.server.web.page.account.overview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.gitplex.commons.wicket.behavior.OnTypingDoneBehavior;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.manager.DepotManager;
import com.gitplex.server.core.security.SecurityUtils;
import com.gitplex.server.web.Constants;
import com.gitplex.server.web.page.depot.file.DepotFilePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class DepotListPanel extends GenericPanel<Account> {

	private PageableListView<Depot> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	private String searchInput;
	
	public DepotListPanel(String id, IModel<Account> accountModel) {
		super(id, accountModel);
	}

	private Account getAccount() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("searchDepots", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(depotsContainer);
				target.add(noDepotsContainer);
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
				setResponsePage(NewDepotPage.class, NewDepotPage.paramsOf(getAccount()));
			}
			
		});
		
		depotsContainer = new WebMarkupContainer("depotsContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!depotsView.getModelObject().isEmpty());
			}
			
		};
		depotsContainer.setOutputMarkupPlaceholderTag(true);
		add(depotsContainer);
		
		noDepotsContainer = new WebMarkupContainer("noDepots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getModelObject().isEmpty());
			}
			
		};
		noDepotsContainer.setOutputMarkupPlaceholderTag(true);
		add(noDepotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<Depot>("depots", 
				new LoadableDetachableModel<List<Depot>>() {

			@Override
			protected List<Depot> load() {
				List<Depot> depots = new ArrayList<>();
				for (Depot depot: GitPlex.getInstance(DepotManager.class)
						.findAllAccessible(getAccount(), SecurityUtils.getAccount())) {
					if (depot.matches(searchInput)) {
						depots.add(depot);
					}
				}
				
				Collections.sort(depots);
				return depots;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Depot> item) {
				Depot depot = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("depotLink", 
						DepotFilePage.class, DepotFilePage.paramsOf(depot)); 
				link.add(new Label("depotName", depot.getName()));
				item.add(link);
						
				item.add(new Label("public", depot.isPublicRead()?"yes":"no"));
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("depotsPageNav", depotsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
}
