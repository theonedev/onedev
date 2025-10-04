package io.onedev.server.web.component.user.twofactorauthentication;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.web.page.user.UserPage;

public abstract class TwoFactorAuthenticationStatusPanel extends Panel {
	public TwoFactorAuthenticationStatusPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Fragment fragment;
		if (getUser().getTwoFactorAuthentication() != null) {
			fragment = new Fragment("content", "configuredFrag", this);
			fragment.add(new Link<Void>("requestToSetupAgain") {

				@Override
				public void onClick() {
					getUser().setTwoFactorAuthentication(null);
					OneDev.getInstance(UserService.class).update(getUser(), null);
					if (getPage() instanceof UserPage) {
						OneDev.getInstance(AuditService.class).audit(null, "reset two factor authentication of account \"" + getUser().getName() + "\"", null, null);
					}
					setResponsePage(getPage().getPageClass(), getPage().getPageParameters());
				}
			});
		} else {
			fragment = new Fragment("content", "notConfiguredFrag", this);
		}
		add(fragment);
	}

	protected abstract User getUser();
	
}
