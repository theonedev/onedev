package io.onedev.server.web.component.user.ident;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.component.user.card.UserCardPanel;
import io.onedev.server.web.page.admin.user.UserProfilePage;
import io.onedev.server.web.page.my.MyProfilePage;

@SuppressWarnings("serial")
public class UserIdentPanel extends Panel {

	private final Long userId;
	
	private final String displayName;
	
	private final Mode mode;
	
	public UserIdentPanel(String id, User user, Mode mode) {
		super(id);
		userId = user.getId();
		displayName = user.getDisplayName();
		this.mode = mode;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatar("avatar", userId, displayName).setVisible(mode != Mode.NAME));
		add(new Label("name", displayName).setVisible(mode != Mode.AVATAR));
		
		add(AttributeAppender.append("class", "user"));
		if (userId != null) {
			User user = OneDev.getInstance(UserManager.class).load(userId);
			if (SecurityUtils.isAdministrator()) {
				CharSequence url = RequestCycle.get().urlFor(UserProfilePage.class, UserProfilePage.paramsOf(user));
				add(AttributeAppender.append("href", url.toString()));
			} else if (user.equals(SecurityUtils.getUser())) {
				CharSequence url = RequestCycle.get().urlFor(MyProfilePage.class, new PageParameters());
				add(AttributeAppender.append("href", url.toString()));
			}
		}
		
		add(new DropdownHoverBehavior(AlignPlacement.top(8), 350) {

			@Override
			protected Component newContent(String id) {
				return new UserCardPanel(id, userId, displayName);
			}
			
		});
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.setName("a");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IdentCssResourceReference()));
	}

}
