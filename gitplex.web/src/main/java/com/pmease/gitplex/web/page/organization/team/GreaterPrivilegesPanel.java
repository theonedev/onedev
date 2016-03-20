package com.pmease.gitplex.web.page.organization.team;

import java.io.Serializable;
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
import com.pmease.gitplex.web.page.organization.MemberPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;

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
		
		add(new ListView<EffectPrivilege>("privileges", new LoadableDetachableModel<List<EffectPrivilege>>() {

			@Override
			protected List<EffectPrivilege> load() {
				List<EffectPrivilege> effectPrivileges = new ArrayList<>();
				for (Map.Entry<Account, DepotPrivilege> entry: getGreaterPrivileges().entrySet()) {
					effectPrivileges.add(new EffectPrivilege(entry.getKey(), entry.getValue()));
				}
				Collections.sort(effectPrivileges, new Comparator<EffectPrivilege>() {

					@Override
					public int compare(EffectPrivilege effectPrivilege1, EffectPrivilege effectPrivilege2) {
						if (effectPrivilege1.getPrivilege() != effectPrivilege2.getPrivilege()) {
							return effectPrivilege2.getPrivilege().ordinal() - effectPrivilege1.getPrivilege().ordinal();
						} else {
							return effectPrivilege1.getUser().getDisplayName()
									.compareTo(effectPrivilege2.getUser().getDisplayName());
						}
					}
					
				});
				return effectPrivileges;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<EffectPrivilege> item) {
				EffectPrivilege effectPrivilege = item.getModelObject();
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

	private static class EffectPrivilege implements Serializable {
		private final Account user;
		
		private final DepotPrivilege privilege;

		public EffectPrivilege(Account user, DepotPrivilege privilege) {
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
