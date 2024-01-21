package io.onedev.server.web.page.simple.security;

import com.google.common.collect.Sets;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class CreateUserFromInvitationPage extends SimplePage {

	private final String PARAM_INVITATION_CODE = "invitationCode";
	
	private final IModel<UserInvitation> invitationModel;
	
	public CreateUserFromInvitationPage(PageParameters params) {
		super(params);
		
		String invitationCode = params.get(PARAM_INVITATION_CODE).toString();
		invitationModel = new LoadableDetachableModel<UserInvitation>() {

			@Override
			protected UserInvitation load() {
				UserInvitation invitation = getInvitationManager().findByInvitationCode(invitationCode);
				if (invitation == null)
					throw new ExplicitException("Invalid invitation code");
				else if (getEmailAddressManager().findByValue(invitation.getEmailAddress()) != null)
					throw new ExplicitException("Email address already used: " + invitation.getEmailAddress());
				else
					return invitation;
			}
			
		};
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		User newUser = new User();
		BeanEditor editor = BeanContext.edit("editor", newUser, Sets.newHashSet(User.PROP_GUEST), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User userWithSameName = getUserManager().findByName(newUser.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 
				
				if (editor.isValid()){
					User user = new User();
					user.setName(newUser.getName());
					user.setFullName(newUser.getFullName());
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(newUser.getPassword()));
					user.setGuest(invitationModel.getObject().isInviteAsGuest());
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(invitationModel.getObject().getEmailAddress());
					emailAddress.setOwner(user);
					emailAddress.setVerificationCode(null);
					
					OneDev.getInstance(TransactionManager.class).run(() -> {
						getUserManager().create(user);
						getEmailAddressManager().create(emailAddress);
						getInvitationManager().delete(invitationModel.getObject());
					});
					
					Session.get().success("Account set up successfully");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(MyAvatarPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}
	
	private UserInvitationManager getInvitationManager() {
		return OneDev.getInstance(UserInvitationManager.class);
	}

	@Override
	protected void onDetach() {
		invitationModel.detach();
		super.onDetach();
	}

	@Override
	protected String getTitle() {
		return "Set Up Your Account";
	}

	@Override
	protected String getSubTitle() {
		return invitationModel.getObject().getEmailAddress();
	}

}
