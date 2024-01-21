package io.onedev.server.web.page.admin.usermanagement;

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

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.usermanagement.profile.UserProfilePage;
import io.onedev.server.web.util.editablebean.NewUserBean;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {

	private NewUserBean newUserBean = new NewUserBean();
	
	private boolean continueToAdd;
	
	public NewUserPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
				if (editor.isValid()){
					User user = new User();
					user.setName(newUserBean.getName());
					user.setFullName(newUserBean.getFullName());
					user.setGuest(newUserBean.isGuest());
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(newUserBean.getPassword()));
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(newUserBean.getEmailAddress());
					emailAddress.setOwner(user);
					emailAddress.setVerificationCode(null);
					
					OneDev.getInstance(TransactionManager.class).run(new Runnable() {

						@Override
						public void run() {
							getUserManager().create(user);
							getEmailAddressManager().create(emailAddress);
						}
						
					});
					
					Session.get().success("New user created");
					if (continueToAdd) {
						newUserBean = new NewUserBean();
						replace(BeanContext.edit("editor", newUserBean));
					} else {
						setResponsePage(UserProfilePage.class, UserProfilePage.paramsOf(user));
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

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
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
