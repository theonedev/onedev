package io.onedev.server.web.page.simple.security;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.simple.SimplePage;
import io.onedev.server.web.util.editablebean.NewUserBean;

@SuppressWarnings("serial")
public class SignUpPage extends SimplePage {
	
	public SignUpPage(PageParameters params) {
		super(params);
		
		if (!OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfRegister())
			throw new UnauthenticatedException("User sign-up is disabled");
		if (getLoginUser() != null)
			throw new IllegalStateException("Can not sign up a user while signed in");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		NewUserBean newUserBean = new NewUserBean();
		BeanEditor editor = BeanContext.edit("editor", newUserBean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User userWithSameName = getUserManager().findByName(newUserBean.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 
				
				if (getEmailAddressManager().findByValue(newUserBean.getEmailAddress()) != null) {
					editor.error(new Path(new PathNode.Named(NewUserBean.PROP_EMAIL_ADDRESS)),
							"Email address already used by another user");
				} 
				if (editor.isValid()) {
					User user = new User();
					user.setName(newUserBean.getName());
					user.setFullName(newUserBean.getFullName());
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(newUserBean.getPassword()));
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(newUserBean.getEmailAddress());
					emailAddress.setOwner(user);
					
					OneDev.getInstance(TransactionManager.class).run(new Runnable() {

						@Override
						public void run() {
							getUserManager().save(user);
							getEmailAddressManager().save(emailAddress);
						}
						
					});
					
					Session.get().success("Account sign up successfully");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(HomePage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(HomePage.class);
			}
			
		});
		add(form);
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}

	@Override
	protected String getTitle() {
		return "Sign Up";
	}

	@Override
	protected String getSubTitle() {
		return "Enter your details to create your account";
	}

}
