package io.onedev.server.web.component.user.ident;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.User;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.card.UserCardPanel;

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
