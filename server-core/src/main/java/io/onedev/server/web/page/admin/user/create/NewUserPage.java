package io.onedev.server.web.page.admin.user.create;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.UserCssResourceReference;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {

	private User user = new User();
	
	private boolean continueToAdd;
	
	public NewUserPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.edit("editor", user);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				User userWithSameName = userManager.findByName(user.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another user.");
				} 
				User userWithSameEmail = userManager.findByEmail(user.getEmail());
				if (userWithSameEmail != null) {
					editor.error(new Path(new PathNode.Named("email")),
							"This email has already been used by another user.");
				} 
				if (editor.isValid()){
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					userManager.save(user, null);
					Session.get().success("New user created");
					if (continueToAdd) {
						user = new User();
						replace(BeanContext.edit("editor", user));
					} else {
						setResponsePage(UserMembershipsPage.class, UserMembershipsPage.paramsOf(user));
					}
				}
			}
			
		};
		form.add(editor);
		form.add(new CheckBox("continueToAdd", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return continueToAdd;
			}

			@Override
			public void setObject(Boolean object) {
				continueToAdd = object;
			}
			
		}));
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Create User");
	}

}
