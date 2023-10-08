package io.onedev.server.web.component.user.card;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

@SuppressWarnings("serial")
public class UserCardPanel extends GenericPanel<User> {

	public UserCardPanel(String id, User user) {
		super(id);
		
		Long userId = user.getId();
		setModel(new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return OneDev.getInstance(UserManager.class).load(userId);
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
		builder.append("<div>" + HtmlEscape.escapeHtml5(getUser().getDisplayName()) + "</div>");
		
		if (getUser().isUnknown()) 
			builder.append("<i>Unknown Account</i>");
		else if (getUser().isSystem()) 
			builder.append("<i>System Account</i>");
		else 
			builder.append("<i>@" + HtmlEscape.escapeHtml5(getUser().getName()) + "</i>");
		container.add(new Label("info", builder.toString()).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCardCssResourceReference()));
	}

}
