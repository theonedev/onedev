package io.onedev.server.web.component.user.detail;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.userident.GitUserIdent;
import io.onedev.server.util.userident.OrdinaryUserIdent;
import io.onedev.server.util.userident.SystemUserIdent;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.link.EmailLink;
import io.onedev.server.web.component.user.avatar.UserAvatar;

@SuppressWarnings("serial")
public class UserDetailPanel extends Panel {

	private final UserIdent userIdent;
	
	public UserDetailPanel(String id, UserIdent userIdent) {
		super(id);
		Preconditions.checkArgument(!(userIdent instanceof SystemUserIdent));
		this.userIdent = userIdent;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (userIdent instanceof GitUserIdent) {
			GitUserIdent gitUserIdent = (GitUserIdent) userIdent;
			Fragment fragment = new Fragment("content", "gitUserFrag", this);
			fragment.add(new UserAvatar("avatar", userIdent));
			
			String label = HtmlEscape.escapeHtml5(userIdent.getName()) + " <i>(" + gitUserIdent.getGitRole() + ")</i>"; 
			fragment.add(new Label("gitName", label).setEscapeModelStrings(false));
			
			User user = OneDev.getInstance(UserManager.class).findByEmail(gitUserIdent.getEmail());
			if (user != null) {
				label = HtmlEscape.escapeHtml5(user.getName()) + " <i>(Account in OneDev)</i>"; 
				fragment.add(new Label("onedevName", label).setEscapeModelStrings(false));
			} else {
				fragment.add(new Label("onedevName", "<i>No OneDev account</i>").setEscapeModelStrings(false));
			}
			fragment.add(new EmailLink("email", Model.of(gitUserIdent.getEmail())));
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "nonGitUserFrag", this);
			fragment.add(new UserAvatar("avatar", userIdent));
			if (userIdent instanceof OrdinaryUserIdent) {
				fragment.add(new Label("name", userIdent.getName()));
				fragment.add(new EmailLink("email", Model.of(((OrdinaryUserIdent)userIdent).getEmail())));
			} else {
				fragment.add(new Label("name", userIdent.getName()).setEscapeModelStrings(false));
				fragment.add(new Label("email", "<i>Account is removed</i>") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("span");
					}
					
				}.setEscapeModelStrings(false));
			}
			add(fragment);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserDetailCssResourceReference()));
	}

}
