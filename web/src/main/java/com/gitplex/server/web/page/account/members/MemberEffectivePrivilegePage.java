package com.gitplex.server.web.page.account.members;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.DropdownLink;
import com.gitplex.server.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.page.depot.setting.authorization.DepotEffectivePrivilegePage;
import com.gitplex.server.web.util.depotaccess.DepotAccess;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class MemberEffectivePrivilegePage extends MemberPage {

	private PageableListView<DepotPermission> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	private DepotPrivilege filterPrivilege;
	
	private String searchInput;
	
	public MemberEffectivePrivilegePage(PageParameters params) {
		super(params);
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
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}
			
		});
		
		WebMarkupContainer filterContainer = new WebMarkupContainer("filter");
		filterContainer.setOutputMarkupId(true);
		add(filterContainer);
		
		filterContainer.add(new DropdownLink("selection") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (filterPrivilege == null)
							return "Filter by privilege";
						else 
							return filterPrivilege.toString();
					}
					
				}));
			}

			@Override
			protected Component newContent(String id) {
				return new PrivilegeSelectionPanel(id, true, filterPrivilege) {

					@Override
					protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
						close();
						filterPrivilege = privilege;
						target.add(filterContainer);
						target.add(depotsContainer);
						target.add(pagingNavigator);
						target.add(noDepotsContainer);
					}

				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterPrivilege = null;
				target.add(filterContainer);
				target.add(depotsContainer);
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterPrivilege != null);
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
		
		depotsContainer.add(depotsView = new PageableListView<DepotPermission>("depots", 
				new LoadableDetachableModel<List<DepotPermission>>() {

			@Override
			protected List<DepotPermission> load() {
				List<DepotPermission> permissions = new ArrayList<>();
				
				Account user = getMembership().getUser();
				for (Depot depot: getAccount().getDepots()) {
					if (depot.matches(searchInput)) {
						DepotPrivilege privilege = new DepotAccess(user, depot).getGreatestPrivilege();
						if (privilege != DepotPrivilege.NONE 
								&& (filterPrivilege == null || filterPrivilege == privilege)) {
							permissions.add(new DepotPermission(depot, privilege));
						}
					}
				}
				
				permissions.sort((permission1, permission2) 
						-> permission1.getDepot().compareTo(permission2.getDepot()));
				return permissions;
			}
			
		}, WebConstants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<DepotPermission> item) {
				DepotPermission permission = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>(
						"depotLink", 
						DepotEffectivePrivilegePage.class, 
						DepotEffectivePrivilegePage.paramsOf(permission.getDepot()));
				link.add(new Label("name", permission.getDepot().getName()));
				item.add(link);

				link = new BookmarkablePageLink<Void>(
						"privilegeLink", 
						MemberPrivilegeSourcePage.class, 
						MemberPrivilegeSourcePage.paramsOf(getMembership(), permission.getDepot()));
				link.add(new Label("privilege", permission.getPrivilege().toString()));
				item.add(link);
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", depotsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		noDepotsContainer = new WebMarkupContainer("noDepots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getModelObject().isEmpty());
			}
			
		};
		noDepotsContainer.setOutputMarkupPlaceholderTag(true);
		add(noDepotsContainer);
	}

	/*
	 * Member effective privilege page is only visible to administrator as it contains repository 
	 * authorization information and we do not want to expose that information to ordinary members 
	 * as repository name might also be a secret
	 */
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
	private static class DepotPermission {
		
		private final Depot depot;
		
		private final DepotPrivilege privilege;
		
		public DepotPermission(Depot depot, DepotPrivilege privilege) {
			this.depot = depot;
			this.privilege = privilege;
		}

		public Depot getDepot() {
			return depot;
		}

		public DepotPrivilege getPrivilege() {
			return privilege;
		}
		
	}

}
