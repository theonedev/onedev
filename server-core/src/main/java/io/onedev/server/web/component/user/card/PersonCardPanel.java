package io.onedev.server.web.component.user.card;

import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.text.MessageFormat;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.OneDev;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

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
		
		EmailAddressService emailAddressService = OneDev.getInstance(EmailAddressService.class);
		String displayName;
		EmailAddress emailAddress = emailAddressService.findByValue(personIdent.getEmailAddress());
		if (emailAddress != null && emailAddress.isVerified())
			displayName = emailAddress.getOwner().getDisplayName();
		else
			displayName = personIdent.getName();
		
		builder.append("<div>" + escapeHtml5(displayName) + " <i>(" + gitRole + ")</i></div>");
		
		if (personIdent.getName().equals(User.SYSTEM_NAME)) 
			builder.append("<i>System Account</i>");
		else if (emailAddress != null && emailAddress.isVerified()) 
			builder.append(MessageFormat.format("<a href=\"/~users/{0}\">@{1}</a>", emailAddress.getOwner().getId(), escapeHtml5(emailAddress.getOwner().getName()))); 
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
