package io.onedev.server.web.component.user.card;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

@SuppressWarnings("serial")
public class UserCardPanel extends Panel {

	private final Long userId;
	
	private final String displayName;
	
	public UserCardPanel(String id, @Nullable Long userId, String displayName) {
		super(id);
		this.userId = userId;
		this.displayName = displayName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);

		container.add(new UserAvatar("avatar", userId, displayName));
		
		StringBuilder builder = new StringBuilder();
		builder.append("<div>" + HtmlEscape.escapeHtml5(displayName) + "</div>");
		
		if (userId == null) {
			builder.append("<i>No OneDev account</i>");
		} else if (User.SYSTEM_ID.equals(userId)) {
			builder.append("<i>System Account</i>");
		} else {
			User user = OneDev.getInstance(UserManager.class).load(userId);
			builder.append("<i>@" + HtmlEscape.escapeHtml5(user.getName()) + "</i>");
		}
		container.add(new Label("info", builder.toString()).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCardCssResourceReference()));
	}

}
