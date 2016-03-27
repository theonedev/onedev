package com.pmease.gitplex.web.page.account.members;

import static com.pmease.gitplex.web.page.account.members.RoleSelectionPanel.ROLE_ADMIN;
import static com.pmease.gitplex.web.page.account.members.RoleSelectionPanel.ROLE_MEMBER;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public abstract class MemberPage extends AccountLayoutPage {

	private static final String PARAM_MEMBER = "member";
	
	private final IModel<OrganizationMembership> membershipModel;
	
	public MemberPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
		
		String userName = params.get(PARAM_MEMBER).toString();
		Account user = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).findByName(userName));
		membershipModel = new LoadableDetachableModel<OrganizationMembership>() {

			@Override
			protected OrganizationMembership load() {
				return Preconditions.checkNotNull(
						GitPlex.getInstance(OrganizationMembershipManager.class).find(getAccount(), user));
			}
			
		};
	}

	protected OrganizationMembership getMembership() {
		return membershipModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("memberAvatar", getMembership().getUser()));
		add(new UserLink("memberLink", getMembership().getUser()));
		add(new DropdownLink("memberRole") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setBeforeDisabledLink("");
				setAfterDisabledLink("");
				
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (getMembership().isAdmin())
							return "Role: "+ ROLE_ADMIN;
						else
							return "Role: " + ROLE_MEMBER;
					}
					
				}));
				Account user = getMembership().getUser();
				if (!SecurityUtils.canManage(getAccount()) || user.equals(getLoginUser()))
					add(AttributeAppender.append("disabled", "disabled"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				Account user = getMembership().getUser();
				setEnabled(SecurityUtils.canManage(getAccount()) && !user.equals(getLoginUser()));
			}

			@Override
			protected Component newContent(String id) {
				return new RoleSelectionPanel(id, getMembership().isAdmin()?ROLE_ADMIN:ROLE_MEMBER) {
					
					@Override
					protected void onSelectOrdinary(AjaxRequestTarget target) {
						close();
						getMembership().setAdmin(false);
						GitPlex.getInstance(OrganizationMembershipManager.class).save(getMembership());
						setResponsePage(getPageClass(), getPageParameters());
						Session.get().success("Role updated");
					}
					
					@Override
					protected void onSelectAdmin(AjaxRequestTarget target) {
						close();
						getMembership().setAdmin(true);
						GitPlex.getInstance(OrganizationMembershipManager.class).save(getMembership());
						setResponsePage(getPageClass(), getPageParameters());
						Session.get().success("Role updated");
					}
					
				};
			}
			
		});
		
		String confirmMessage = String.format("Do you really want to remove user '%s' from organization '%s'?", 
				getMembership().getUser().getDisplayName(), getAccount().getDisplayName());
		add(new Link<Void>("removeMember") {

			@Override
			public void onClick() {
				GitPlex.getInstance(OrganizationMembershipManager.class).delete(getMembership());
				setResponsePage(MemberListPage.class, MemberListPage.paramsOf(getAccount()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}
			
		}.add(new ConfirmOnClick(confirmMessage)));
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Joined Teams"), MemberTeamListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, MemberTeamListPage.class, 
								MemberTeamListPage.paramsOf(getMembership()));
					}
					
				};
			}
			
		});
		
		tabs.add(new PageTab(Model.of("Effective Privileges"), 
				MemberEffectivePrivilegePage.class, MemberPrivilegeSourcePage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new BookmarkablePageLink<Void>(linkId, MemberEffectivePrivilegePage.class, 
								MemberEffectivePrivilegePage.paramsOf(getMembership()));
					}
					
				};
			}
			
		});
		
		add(new Tabbable("memberTabs", tabs).setVisible(SecurityUtils.canManage(getAccount())));
	}

	public static PageParameters paramsOf(OrganizationMembership membership) {
		PageParameters params = paramsOf(membership.getOrganization());
		params.set(PARAM_MEMBER, membership.getUser().getName());
		return params;
	}
	
	@Override
	protected void onDetach() {
		membershipModel.detach();
		super.onDetach();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isMemberOf(getAccount());
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(MemberListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
