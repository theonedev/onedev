package io.onedev.server.web.component.user.ident;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.OneDev;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.card.PersonCardPanel;
import io.onedev.server.web.page.user.profile.UserProfilePage;

public class PersonIdentPanel extends Panel {

	private final PersonIdent personIdent;
	
	private final String gitRole;
	
	private final Mode mode;

	private final IModel<User> userModel;
	
	public PersonIdentPanel(String id, PersonIdent personIdent, String gitRole, Mode mode) {
		super(id);
		this.personIdent = personIdent;
		this.gitRole = gitRole;
		this.mode = mode;
		userModel = new LoadableDetachableModel<User>() {
			@Override
			protected User load() {
				EmailAddressService emailAddressService = OneDev.getInstance(EmailAddressService.class);
				EmailAddress emailAddress = emailAddressService.findByValue(personIdent.getEmailAddress());
				if (emailAddress != null && emailAddress.isVerified())
					return emailAddress.getOwner();
				else
					return null;
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatar("avatar", personIdent).setVisible(mode != Mode.NAME));
		
		var user = userModel.getObject();
		if (user != null)
			add(new Label("name", user.getDisplayName()).setVisible(mode != Mode.AVATAR));
		else
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
		var user = userModel.getObject();
		if (user != null && user.getId() > 0) {
			tag.setName("a");
			var url = RequestCycle.get().urlFor(UserProfilePage.class, UserProfilePage.paramsOf(user));
			tag.put("href", url.toString());		
		} else {
			tag.setName("span");
		}
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IdentCssResourceReference()));
	}

}
