package com.gitplex.server.web.page.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

import com.gitplex.commons.wicket.behavior.OnTypingDoneBehavior;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.manager.DepotManager;
import com.gitplex.server.web.Constants;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.page.layout.LayoutPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

	private PageableListView<Depot> depotsView;
	
	private BootstrapPagingNavigator depotsPageNav;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	private String searchInput;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("search", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(depotsContainer);
				target.add(depotsPageNav);
				target.add(noDepotsContainer);
			}

		});
		
		depotsContainer = new WebMarkupContainer("depots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!depotsView.getModelObject().isEmpty());
			}
			
		};
		depotsContainer.setOutputMarkupPlaceholderTag(true);
		add(depotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<Depot>("depots", 
				new LoadableDetachableModel<List<Depot>>() {

			@Override
			protected List<Depot> load() {
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				List<Depot> depots = new ArrayList<>();
				for (Depot depot: depotManager.findAllAccessible(null, getLoginUser())) {
					if (depot.matchesFQN(searchInput)) {
						depots.add(depot);
					}
				}
				depots.sort(Depot::compareLastVisit);
				return depots;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Depot> item) {
				Depot depot = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", 
						DepotFilePage.class, DepotFilePage.paramsOf(depot)); 
				link.add(new Label("name", depot.getFQN()));
				item.add(link);
			}
			
		});

		add(depotsPageNav = new BootstrapAjaxPagingNavigator("depotsPageNav", depotsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getPageCount() > 1);
			}
			
		});
		depotsPageNav.setOutputMarkupPlaceholderTag(true);
		
		add(noDepotsContainer = new WebMarkupContainer("noDepots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getModelObject().isEmpty());
			}
			
		});
		noDepotsContainer.setOutputMarkupPlaceholderTag(true);		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DashboardResourceReference()));
	}

	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Accessible Repositories");
	}

}
