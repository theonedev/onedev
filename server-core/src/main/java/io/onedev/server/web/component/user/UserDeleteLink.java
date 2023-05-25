package io.onedev.server.web.component.user;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.admin.usermanagement.UserListPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;

@SuppressWarnings("serial")
public abstract class UserDeleteLink extends Link<Void> {

	public UserDeleteLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getUser().equals(SecurityUtils.getUser()))
			add(new ConfirmClickModifier("Do you really want to remove your account?"));		
		else
			add(new ConfirmClickModifier("Do you really want to delete user '" + getUser().getDisplayName() + "'?"));
	}

	@Override
	public void onClick() {
		if (getUser().equals(SecurityUtils.getUser())) {
			OneDev.getInstance(UserManager.class).delete(getUser());
			WebSession.get().logout();
			WebSession.get().warn("Your account is deleted");
			throw new RestartResponseException(getApplication().getHomePage());
		} else {
			OneDev.getInstance(UserManager.class).delete(getUser());
			WebSession.get().success("User '" + getUser().getDisplayName() + "' deleted");
			String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(User.class);
			if (redirectUrlAfterDelete != null)
				throw new RedirectToUrlException(redirectUrlAfterDelete);
			else
				setResponsePage(UserListPage.class);
		}
	}

	protected abstract User getUser();
	
}
