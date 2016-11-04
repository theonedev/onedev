package com.gitplex.server.web.page.account.setting;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.security.SecurityUtils;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public class PasswordEditPage extends AccountSettingPage {
	
	public PasswordEditPage(PageParameters params) {
		super(params);
		Preconditions.checkState(!getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PasswordEditBean bean = new PasswordEditBean();
		
		Set<String> excludedProperties = new HashSet<>();
		
		// in case administrator changes password for non-adminsitrator user, we do not 
		// ask for old password
		if (SecurityUtils.getAccount().isAdministrator() && !getAccount().isAdministrator()) 
			excludedProperties.add("oldPassword");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getAccount().setPassword(bean.getNewPassword());
				GitPlex.getInstance(AccountManager.class).save(getAccount(), null);
				Session.get().success("Password has been changed");

				bean.setOldPassword(null);
				replace(BeanContext.editBean("editor", bean, excludedProperties));
			}

		};
		add(form);
		
		form.add(BeanContext.editBean("editor", bean, excludedProperties));
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(PasswordEditPage.class, paramsOf(account));
	}
	
}
