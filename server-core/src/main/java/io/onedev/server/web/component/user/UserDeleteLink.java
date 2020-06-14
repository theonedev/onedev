package io.onedev.server.web.component.user;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public abstract class UserDeleteLink extends Link<Void> {

	public UserDeleteLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ConfirmClickModifier("Do you really want to delete user '" + getUser().getDisplayName() + "'?"));		
	}

	@Override
	public void onClick() {
		OneDev.getInstance(UserManager.class).delete(getUser());
		WebSession.get().success("User '" + getUser().getDisplayName() + "' deleted");
		
		String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(User.class);
		if (redirectUrlAfterDelete != null)
			throw new RedirectToUrlException(redirectUrlAfterDelete);
		else
			setResponsePage(UserListPage.class);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.isAdministrator() 
				&& !getUser().isRoot() 
				&& !getUser().equals(SecurityUtils.getUser()));
	}

	protected abstract User getUser();
	
}
