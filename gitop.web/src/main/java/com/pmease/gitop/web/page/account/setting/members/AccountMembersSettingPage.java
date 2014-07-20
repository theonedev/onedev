package com.pmease.gitop.web.page.account.setting.members;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;

@SuppressWarnings("serial")
public class AccountMembersSettingPage extends AccountSettingPage {

	public AccountMembersSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Members - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
		
		add(new BookmarkablePageLink<Void>("teamlink", 
				AccountTeamsPage.class, AccountTeamsPage.paramsOf(getAccount())));
		
		add(new MemberListView("members", new UserModel(getAccount()), model){
			@Override
			protected Component createActionsPanel(String id, IModel<User> model) {
				Fragment frag = new Fragment(id, "memberactionfrag", AccountMembersSettingPage.this);
				User user = model.getObject();
				if (Objects.equal(user, getAccount())) {
					frag.add(new WebMarkupContainer("remove").setVisibilityAllowed(false));
				} else {
					frag.add(new AjaxLink<User>("remove", new UserModel(user)) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							Dao dao = Gitop.getInstance(Dao.class);
							User user = (User) getDefaultModelObject();
							List<Membership> memberships = dao.query(EntityCriteria.of(Membership.class).add(Restrictions.eq("user", user)));
							for (Membership each : memberships) {
								dao.remove(each);
							}
							
							target.add(AccountMembersSettingPage.this.get("members"));
						}
					}.add(new ConfirmBehavior("<p>Are you sure you want to remove below user? "
							+ "Remove this user will remove her/him from <b>all teams</b></p>"
							+ "<p><b>" 
							+ StringEscapeUtils.escapeHtml4(user.getName()) + "</b> (" 
							+ StringEscapeUtils.escapeHtml4(StringUtils.defaultString(user.getFullName(), " ")) 
							+ ")</p>")));
				}
				
				return frag;
			}
		}.setOutputMarkupId(true));
	}
	
}
