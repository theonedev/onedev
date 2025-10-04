package io.onedev.server.web.component.user.card;

import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.text.MessageFormat;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

public class UserCardPanel extends GenericPanel<User> {

	public UserCardPanel(String id, User user) {
		super(id);
		
		Long userId = user.getId();
		setModel(new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return OneDev.getInstance(UserService.class).load(userId);
			}
			
		});
	}

	private User getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);

		container.add(new UserAvatar("avatar", getUser()));
		
		StringBuilder builder = new StringBuilder();
		builder.append("<div>" + escapeHtml5(getUser().getDisplayName()) + "</div>");
		
		if (getUser().isUnknown()) 
			builder.append("<i>Unknown Account</i>");
		else if (getUser().isSystem()) 
			builder.append("<i>System Account</i>");
		else 
			builder.append(MessageFormat.format("<a href=\"/~users/{0}\">@{1}</a>", getUser().getId(), escapeHtml5(getUser().getName())));
		container.add(new Label("info", builder.toString()).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCardCssResourceReference()));
	}

}
