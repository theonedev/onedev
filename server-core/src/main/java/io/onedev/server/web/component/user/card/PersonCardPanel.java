package io.onedev.server.web.component.user.card;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.avatar.UserAvatar;

@SuppressWarnings("serial")
public class PersonCardPanel extends Panel {

	private final PersonIdent personIdent;
	
	private final String gitRole;
	
	public PersonCardPanel(String id, PersonIdent personIdent, String gitRole) {
		super(id);
		this.personIdent = personIdent;
		this.gitRole = gitRole;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		container.add(new UserAvatar("avatar", personIdent));
		
		StringBuilder builder = new StringBuilder();
		builder.append("<div>" + HtmlEscape.escapeHtml5(personIdent.getName()) + 
				" <i>(" + gitRole + ")</i></div>");
		
		if (StringUtils.isBlank(personIdent.getEmailAddress())) {
			if (personIdent.getName().equals(OneDev.NAME))
				builder.append("<i>System Account</i>");
			else
				builder.append("<i>No Email Address</i>");
		} else {
			User user = OneDev.getInstance(UserManager.class).findByEmail(personIdent.getEmailAddress());
			if (user != null) 
				builder.append("<div>" + HtmlEscape.escapeHtml5(user.getName()) + " <i>(Account in OneDev)</i>"); 
			else 
				builder.append("<i>No OneDev Account</i>");
			builder.append("<div><a href='mailto:" + personIdent.getEmailAddress() + "'>" 
					+ HtmlEscape.escapeHtml5(personIdent.getEmailAddress()) + "</a></div>");
			container.add(AttributeAppender.append("class", "bigger"));
		}
		container.add(new Label("info", builder.toString()).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCardCssResourceReference()));
	}

}
