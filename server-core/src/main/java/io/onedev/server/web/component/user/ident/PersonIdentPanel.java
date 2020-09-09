package io.onedev.server.web.component.user.ident;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.card.PersonCardPanel;

@SuppressWarnings("serial")
public class PersonIdentPanel extends Panel {

	private final PersonIdent personIdent;
	
	private final String gitRole;
	
	private final Mode mode;
	
	public PersonIdentPanel(String id, PersonIdent personIdent, String gitRole, Mode mode) {
		super(id);
		this.personIdent = personIdent;
		this.gitRole = gitRole;
		this.mode = mode;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatar("avatar", personIdent).setVisible(mode != Mode.NAME));
		add(new Label("name", personIdent.getName()).setVisible(mode != Mode.AVATAR));
		
		add(AttributeAppender.append("class", "user"));
		
		add(new DropdownHoverBehavior(AlignPlacement.top(8), 350) {

			@Override
			protected Component newContent(String id) {
				return new PersonCardPanel(id, personIdent, gitRole);
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
