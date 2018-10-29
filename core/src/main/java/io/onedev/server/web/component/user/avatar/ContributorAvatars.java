package io.onedev.server.web.component.user.avatar;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

@SuppressWarnings("serial")
public class ContributorAvatars extends Panel {

	private final PersonIdent author;
	
	private final PersonIdent committer;
	
	public ContributorAvatars(String id, PersonIdent author, PersonIdent committer) {
		super(id);
		this.author = author;
		this.committer = committer;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserAvatarLink("author", author));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new UserAvatarLink("committer", committer));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserAvatarResourceReference()));
	}

}
