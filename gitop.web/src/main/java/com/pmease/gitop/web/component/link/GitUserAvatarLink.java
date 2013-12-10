package com.pmease.gitop.web.component.link;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Optional;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.component.avatar.GravatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;
import com.pmease.gitop.web.page.project.api.GitPerson;

/**
 * Link for git author/committer, display avatar only
 *
 */
public class GitUserAvatarLink extends Panel {
	private static final long serialVersionUID = 1L;

	public GitUserAvatarLink(String id, IModel<GitPerson> model) {
		super(id, model);
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Optional<User> user = getPerson().asUser();
		if (user.isPresent()) {
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", 
					AccountHomePage.class, 
					PageSpec.forUser(user.get()));
			
			add(link);
			link.add(AttributeModifier.replace("title", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return getPerson().asUser().get().getName();
				}
			}));
			
			link.add(new AvatarImage("avatar", new UserModel(user.get())));
		} else {
			AbstractLink link = new AbstractLink("link") {};
			add(link);
			link.setEnabled(false);
			link.add(AttributeModifier.replace("title", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return getPerson().getName();
				}
				
			}));
			
			Fragment frag = new Fragment("avatar", "avatarfrag", this);
			frag.add(new GravatarImage("avatar", Model.of(getPerson().getEmailAddress())));
			link.add(frag);
		}
	}
	
	private GitPerson getPerson() {
		return (GitPerson) getDefaultModelObject();
	}
}
