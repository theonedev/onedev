package io.onedev.server.web.component.user.ident;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.component.user.detail.UserDetailPanel;

@SuppressWarnings("serial")
public class UserIdentPanel extends Panel {

	public enum Mode {AVATAR, NAME, AVATAR_AND_NAME};
	
	private final UserIdent userIdent;
	
	private final Mode mode;
	
	public UserIdentPanel(String id, UserIdent userIdent, Mode mode) {
		super(id);
		this.userIdent = userIdent;
		this.mode = mode;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatar("avatar", userIdent).setVisible(mode != Mode.NAME));
		
		add(new Label("name", userIdent.getName()).setVisible(mode != Mode.AVATAR));
		
		add(AttributeAppender.append("class", "user"));
		
		add(new DropdownHoverBehavior(AlignPlacement.top(8), 350) {

			@Override
			protected Component newContent(String id) {
				return new UserDetailPanel(id, userIdent);
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
		response.render(CssHeaderItem.forReference(new UserIdentCssResourceReference()));
	}

}
