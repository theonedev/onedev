package com.pmease.gitop.web.page.account.setting.members;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;

@SuppressWarnings("serial")
public class AccountMembersSettingPage extends AccountSettingPage {

	@Override
	protected Category getSettingCategory() {
		return Category.MEMBERS;
	}

	@Override
	protected String getPageTitle() {
		return "Members - " + getAccount();
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		final IModel<List<User>> model = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				User account = getAccount();
				Collection<Team> teams = account.getTeams();
				Set<User> users = Sets.newHashSet();
				
				for (Team each : teams) {
					for (Membership membership : each.getMemberships()) {
						users.add(membership.getUser());
					}
				}
				
				List<User> result = Lists.newArrayList(users);
				Collections.sort(result, new Comparator<User>() {

					@Override
					public int compare(User o1, User o2) {
						return o1.getName().compareTo(o2.getName());
					}
					
				});
				return result;
			}
			
		};
		
		add(new BookmarkablePageLink<Void>("teamlink", AccountTeamsPage.class));
		
		add(new MemberListView("members", new UserModel(getAccount()), model){
			@Override
			protected Component createActionsPanel(String id, IModel<User> model) {
				Fragment frag = new Fragment(id, "memberactionfrag", AccountMembersSettingPage.this);
				User user = model.getObject();
				final IModel<User> userModel = new UserModel(user);
				frag.add(new AjaxConfirmLink<Void>("remove",
						Model.of("<p>Are you sure you want to remove below user? "
								+ "Remove this user will remove her/him from <b>all teams</b></p>"
								+ "<p><b>" + user.getName() + "</b> (" + user.getDisplayName() + ")</p>")) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						MembershipManager mm = Gitop.getInstance(MembershipManager.class);
						User user = userModel.getObject();
						List<Membership> memberships = mm.query(Restrictions.eq("user", user));
						for (Membership each : memberships) {
							mm.delete(each);
						}
						
						target.add(AccountMembersSettingPage.this.get("members"));
					}
				});
				return frag;
			}
		}.setOutputMarkupId(true));
	}
	
}
