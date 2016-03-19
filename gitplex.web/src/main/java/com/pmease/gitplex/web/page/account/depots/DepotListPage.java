package com.pmease.gitplex.web.page.account.depots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteDepotModal;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class DepotListPage extends AccountLayoutPage {

	private PageableListView<Depot> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	public DepotListPage(PageParameters params) {
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
		
		add(searchField = new ClearableTextField<String>("searchDepots", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
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
				Depot depot = new Depot();
				depot.setAccount(getAccount());
				setResponsePage(new NewDepotPage(depot));
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
		
		noDepotsContainer = new WebMarkupContainer("noDepotsContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getModelObject().isEmpty());
			}
			
		};
		noDepotsContainer.setOutputMarkupPlaceholderTag(true);
		add(noDepotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<Depot>("depots", new LoadableDetachableModel<List<Depot>>() {

			@Override
			protected List<Depot> load() {
				List<Depot> depots = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (Depot depot: getAccount().getDepots()) {
					if (depot.getName().toLowerCase().contains(searchInput) && SecurityUtils.canRead(depot))
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
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Depot> item) {
				Depot depot = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<>("depotLink", DepotFilePage.class, DepotFilePage.paramsOf(depot)); 
				link.add(new Label("depotName", depot.getName()));
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
				
				item.add(new AjaxLink<Void>("deleteDepot") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteDepotModal(target) {

							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								setResponsePage(DepotListPage.this);
							}

							@Override
							protected Depot getDepot() {
								return item.getModelObject();
							}
							
						};
					}
					
				});
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
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(DepotListPage.class, paramsOf(account));
	}

}
