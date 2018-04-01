package io.onedev.server.web.component.link;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.page.user.UserProfilePage;

@SuppressWarnings("serial")
public class UserLink extends ViewStateAwarePageLink<Void> {

	private final PageParameters params;
	
	private final String name;
	
	public UserLink(String id, @Nullable User user) {
		super(id, UserProfilePage.class);

		if (user == null) {
			params = new PageParameters();
			name = OneDev.NAME;
		} else if (user.getId() == null) {
			params = new PageParameters();
			name = user.getDisplayName();
		} else {
			params = UserPage.paramsOf(user);
			name = user.getDisplayName();
		}
	}
	
	public UserLink(String id, PersonIdent person) {
		super(id, UserProfilePage.class);
		User user = OneDev.getInstance(UserManager.class).find(person);
		if (user != null) { 
			params = UserPage.paramsOf(user);
			name = user.getDisplayName();
		} else {
			name = person.getName();
			params = new PageParameters();
		}
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(name);
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		configure();
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEnabled(!params.isEmpty());
	}

}
