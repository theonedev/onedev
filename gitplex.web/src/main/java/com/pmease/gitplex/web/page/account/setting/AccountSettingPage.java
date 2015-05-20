package com.pmease.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModalBehavior;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class AccountSettingPage extends AccountPage {

	public AccountSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountSettingTab("Profile", "fa fa-fw fa-pencil", ProfileEditPage.class));
		tabs.add(new AccountSettingTab("Password", "fa fa-fw fa-key", PasswordEditPage.class));
		tabs.add(new AccountSettingTab("Avatar", "fa fa-fw fa-photo", AvatarEditPage.class));
		
		add(new Tabbable("tabs", tabs));
		
		ConfirmDeleteAccountModal confirmDeleteDlg = new ConfirmDeleteAccountModal("confirmDeleteDlg") {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				setResponsePage(getApplication().getHomePage());
			}
			
		};
		add(confirmDeleteDlg);
		add(new WebMarkupContainer("delete") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getAccount().isRoot());
			}
			
		}.add(new ConfirmDeleteAccountModalBehavior(confirmDeleteDlg) {

			@Override
			protected User getAccount() {
				return AccountSettingPage.this.getAccount();
			}
			
		}));
	}

}
