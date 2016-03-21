package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Authorization;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;
import com.pmease.gitplex.web.page.organization.member.MemberPage;

@SuppressWarnings("serial")
abstract class GreaterPrivilegesPanel extends GenericPanel<Authorization> {

	private final IModel<Map<Account, DepotPrivilege>> greaterPrivilegesModel = 
			new LoadableDetachableModel<Map<Account, DepotPrivilege>>() {

		@Override
		protected Map<Account, DepotPrivilege> load() {
			return SecurityUtils.getGreaterPrivileges(getAuthorization());
		}
		
	};
	public GreaterPrivilegesPanel(String id, IModel<Authorization> authorizationModel) {
		super(id, authorizationModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(new Label("privilege", getAuthorization().getPrivilege().toString().toLowerCase()));
		add(new Label("depot", getAuthorization().getDepot().getName().toLowerCase()));
		
		add(new ListView<DepotAccess>("privileges", new LoadableDetachableModel<List<DepotAccess>>() {

			@Override
			protected List<DepotAccess> load() {
				List<DepotAccess> depotAccesses = new ArrayList<>();
				for (Map.Entry<Account, DepotPrivilege> entry: getGreaterPrivileges().entrySet()) {
					depotAccesses.add(new DepotAccess(entry.getKey(), entry.getValue()));
				}
				Collections.sort(depotAccesses, new Comparator<DepotAccess>() {

					@Override
					public int compare(DepotAccess depotAccess1, DepotAccess depotAccess2) {
						if (depotAccess1.getPrivilege() != depotAccess2.getPrivilege()) {
							return depotAccess2.getPrivilege().ordinal() - depotAccess1.getPrivilege().ordinal();
						} else {
							return depotAccess1.getUser().getDisplayName()
									.compareTo(depotAccess2.getUser().getDisplayName());
						}
					}
					
				});
				return depotAccesses;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<DepotAccess> item) {
				DepotAccess effectPrivilege = item.getModelObject();
				item.add(new Avatar("avatar", effectPrivilege.getUser()));
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", 
						MemberPage.class, MemberPage.paramsOf(getAuthorization().getDepot().getAccount(), 
								effectPrivilege.getUser().getName()));
				link.add(new Label("name", effectPrivilege.getUser().getDisplayName()));
				item.add(link);
				
				item.add(new Label("privilege", effectPrivilege.getPrivilege().toString()));
			}
			
		});
	}

	private Map<Account, DepotPrivilege> getGreaterPrivileges() {
		return greaterPrivilegesModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		greaterPrivilegesModel.detach();
		super.onDetach();
	}

	private Authorization getAuthorization() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}
	
	protected abstract void onClose(AjaxRequestTarget target);

	private static class DepotAccess {
		
		private final Account user;
		
		private final DepotPrivilege privilege;

		public DepotAccess(Account user, DepotPrivilege privilege) {
			this.user = user;
			this.privilege = privilege;
		}
		
		public Account getUser() {
			return user;
		}

		public DepotPrivilege getPrivilege() {
			return privilege;
		}
		
	}
	
}
