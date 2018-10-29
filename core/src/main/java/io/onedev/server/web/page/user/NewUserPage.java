package io.onedev.server.web.page.user;

import java.util.Set;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class NewUserPage extends LayoutPage {

	private User user = new User();
	
	private CheckBox administratorInput;
	
	private CheckBox canCreateProjectsInput;
	
	private boolean continueToAdd;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Set<String> excludedProperties = Sets.newHashSet("administrator", "canCreateProjects");
		BeanEditor editor = BeanContext.editBean("editor", user, excludedProperties, true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
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
					user.setAdministrator(administratorInput.getModelObject());
					user.setCanCreateProjects(canCreateProjectsInput.getModelObject());
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					userManager.save(user, null);
					Session.get().success("New user created");
					if (continueToAdd) {
						user = new User();
						replace(BeanContext.editBean("editor", user));
					} else {
						setResponsePage(UserListPage.class);
					}
				}
			}
			
		};
		form.add(editor);
		administratorInput = new CheckBox("administrator", Model.of(user.isAdministrator()));
		administratorInput.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (administratorInput.getModelObject())
					canCreateProjectsInput.setModelObject(true);
				target.add(canCreateProjectsInput);
			}
			
		});
		form.add(administratorInput);
		
		canCreateProjectsInput = new CheckBox("canCreateProjects", Model.of(user.isCanCreateProjects())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!administratorInput.getModelObject());
			}
			
		};
		canCreateProjectsInput.setOutputMarkupId(true);
		form.add(canCreateProjectsInput);			
		
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
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

}
