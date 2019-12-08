package io.onedev.server.web.component.user.contributoravatars;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;

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
		
		add(new UserIdentPanel("author", UserIdent.of(author, "Author"), Mode.AVATAR));
		if (committer.getEmailAddress().equals(author.getEmailAddress())
				&& committer.getName().equals(author.getName())) {
			add(new WebMarkupContainer("committer").setVisible(false));
		} else {
			add(new UserIdentPanel("committer", UserIdent.of(committer, "Committer"), Mode.AVATAR));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ContributorAvatarsResourceReference()));
	}

}
