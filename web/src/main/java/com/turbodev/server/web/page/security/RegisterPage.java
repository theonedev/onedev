package com.turbodev.server.web.page.security;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;

import com.turbodev.launcher.loader.AppLoader;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.User;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.editable.BeanEditor;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.page.base.BasePage;
import com.turbodev.server.web.page.dashboard.DashboardPage;
import com.turbodev.server.web.page.user.AvatarEditPage;
import com.turbodev.server.web.page.user.UserPage;

@SuppressWarnings("serial")
public class RegisterPage extends BasePage {
	
	public RegisterPage() {
		if (!TurboDev.getInstance(ConfigManager.class).getSecuritySetting().isEnableSelfRegister())
			throw new UnauthenticatedException("User self-register is disabled");
		if (getLoginUser() != null)
			throw new IllegalStateException("Can not sign up a user while signed in");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		final User user = new User();
		final BeanEditor<?> editor = BeanContext.editBean("editor", user);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = TurboDev.getInstance(UserManager.class);
				User userWithSameName = userManager.findByName(user.getName());
				if (userWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another user.");
				} 
				User userWithSameEmail = userManager.findByEmail(user.getEmail());
				if (userWithSameEmail != null) {
					editor.getErrorContext(new PathSegment.Property("email"))
							.addError("This email has already been used by another user.");
				} 
				if (!editor.hasErrors(true)) {
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					userManager.save(user, null);
					Session.get().success("New user registered");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(AvatarEditPage.class, UserPage.paramsOf(user));
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(DashboardPage.class);
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RegisterResourceReference()));
	}

}
