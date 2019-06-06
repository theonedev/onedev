package io.onedev.server.web.component.user.avatar;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;

import io.onedev.server.OneDev;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class UserAvatar extends WebComponent {

	private String url;
	
	public UserAvatar(String id, UserIdent userIdent) {
		super(id);

		url = getAvatarManager().getAvatarUrl(userIdent);
	}
	
	private AvatarManager getAvatarManager() {
		return OneDev.getInstance(AvatarManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "avatar"));
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
		tag.put("src", url);
	}

}
