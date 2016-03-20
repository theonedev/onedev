package com.pmease.gitplex.web.page.user.setting;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.editable.DefaultBeanDescriptor;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.reflection.ReflectionBeanEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class PasswordEditPage extends AccountSettingPage {
	
	public PasswordEditPage(PageParameters params) {
		super(params);
		Preconditions.checkState(!getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				GitPlex.getInstance(AccountManager.class).save(getAccount(), null);
				Session.get().success("Password has been changed");
			}
			
		};
		add(form);
		
		PasswordDescriptor descriptor = new PasswordDescriptor();
		form.add(new ReflectionBeanEditor("editor", descriptor, new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getAccount();
			}

			@Override
			public void setObject(Serializable object) {
				descriptor.copyProperties(object, getAccount());
			}
			
		}));
	}

	private static class PasswordDescriptor extends DefaultBeanDescriptor {

		public PasswordDescriptor() {
			super(Account.class);
			
			for (Iterator<PropertyDescriptor> it = propertyDescriptors.iterator(); it.hasNext();) {
				if (!it.next().getPropertyName().equals("password"))
					it.remove();
			}
		}
		
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(PasswordEditPage.class, paramsOf(account));
	}
	
}
