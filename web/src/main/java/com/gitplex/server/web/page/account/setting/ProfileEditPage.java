package com.gitplex.server.web.page.account.setting;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class ProfileEditPage extends AccountSettingPage {

	private String oldName;
	
	private BeanEditor<?> editor;
	
	public ProfileEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Set<String> excludedProperties = new HashSet<>();
		excludedProperties.add("password");
		if (getAccount().isOrganization()) {
			excludedProperties.add("email");
		} else {
			excludedProperties.add("description");
			excludedProperties.add("defaultPrivilege");
		}
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getAccount();
			}

			@Override
			public void setObject(Serializable object) {
				// check contract of AccountManager.save on why we assign oldName here
				oldName = getAccount().getName();
				editor.getBeanDescriptor().copyProperties(object, getAccount());
			}
			
		}, excludedProperties);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Account account = getAccount();
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = accountManager.findByName(account.getName());
				if (accountWithSameName != null && !accountWithSameName.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} 
				Account accountWithSameEmail = accountManager.findByEmail(account.getEmail());
				if (accountWithSameEmail != null && !accountWithSameEmail.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("email"))
							.addError("This email has already been used by another account.");
				} 
				if (!editor.hasErrors(true)) {
					accountManager.save(account, oldName);
					Session.get().success("Profile has been updated");
					setResponsePage(ProfileEditPage.class, AccountPage.paramsOf(account));
				}
			}
			
		};
		form.add(editor);

		form.add(new AjaxLink<Void>("delete") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getAccount().isAdministrator());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (!getAccount().getDepots().isEmpty()) {
					target.appendJavaScript("alert('Please delete or transfer repositories under this account first');");
				} else {
					new ConfirmDeleteAccountModal(target) {

						@Override
						protected void onDeleted(AjaxRequestTarget target) {
							setResponsePage(getApplication().getHomePage());
						}

						@Override
						protected Account getAccount() {
							return ProfileEditPage.this.getAccount();
						}
						
					};
				}
			}
			
		});
		
		add(form);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(ProfileEditPage.class, paramsOf(account));
	}

}
