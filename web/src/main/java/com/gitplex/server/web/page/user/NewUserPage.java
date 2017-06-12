package com.gitplex.server.web.page.user;

import java.util.List;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class NewUserPage extends LayoutPage {

	private User user = new User();
	
	private boolean continueToAdd;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", user);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
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
				if (!editor.hasErrors(true)){
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					userManager.save(user, null);
					Session.get().success("New user created");
					if (continueToAdd) {
						user = new User();
						replace(BeanContext.editBean("editor", user));
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
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserResourceReference()));
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, UserListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Users");
					}
					
				};
			}
			
		});

		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				return new Label(componentId, "New User") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("div");
					}
					
				};
			}
			
		});
		
		return breadcrumbs;
	}

}
