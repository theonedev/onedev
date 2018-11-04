package io.onedev.server.web.component.user.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class UserAvatar extends WebComponent {

	private String url;
	
	public UserAvatar(String id, @Nullable User user) {
		super(id);

		url = getAvatarManager().getAvatarUrl(user!=null?user.getFacade():null);
	}
	
	public UserAvatar(String id, PersonIdent person) {
		super(id);
		
		User user = OneDev.getInstance(UserManager.class).find(person);
		if (user != null) { 
			url = getAvatarManager().getAvatarUrl(user.getFacade());
		} else {
			url = getAvatarManager().getAvatarUrl(person);
		}
	}
	
	private AvatarManager getAvatarManager() {
		return OneDev.getInstance(AvatarManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserAvatarResourceReference()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.append("class", "avatar", " ");
		tag.put("src", url);
	}

}
