package com.pmease.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountTab;
import com.pmease.gitplex.web.page.user.setting.PasswordEditPage;

@SuppressWarnings("serial")
public class AccountSettingPage extends AccountLayoutPage {

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
		tabs.add(new AccountTab("Profile", "", ProfileEditPage.class));
		tabs.add(new AccountTab("Avatar", "", AvatarEditPage.class));
		
		if (!getAccount().isOrganization())
			tabs.add(new AccountTab("Password", "", PasswordEditPage.class));
		
		add(new Tabbable("accountSettingTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(AccountSettingPage.class, "account-setting.css")));
	}

}
