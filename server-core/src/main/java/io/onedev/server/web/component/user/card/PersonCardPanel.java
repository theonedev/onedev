package io.onedev.server.web.component.user.card;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

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
		
		EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
		String displayName;
		EmailAddress emailAddress = emailAddressManager.findByValue(personIdent.getEmailAddress());
		if (emailAddress != null && emailAddress.isVerified())
			displayName = emailAddress.getOwner().getDisplayName();
		else
			displayName = personIdent.getName();
		
		builder.append("<div>" + HtmlEscape.escapeHtml5(displayName) + " <i>(" + gitRole + ")</i></div>");
		
		if (personIdent.getName().equals(User.SYSTEM_NAME)) 
			builder.append("<i>System Account</i>");
		else if (emailAddress != null && emailAddress.isVerified()) 
			builder.append("<i>@" + HtmlEscape.escapeHtml5(emailAddress.getOwner().getName()) + "</i>"); 
		else 
			builder.append("<i>No OneDev Account</i>");
		
		container.add(new Label("info", builder.toString()).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCardCssResourceReference()));
	}

}
