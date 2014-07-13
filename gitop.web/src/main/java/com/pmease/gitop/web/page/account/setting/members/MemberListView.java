package com.pmease.gitop.web.page.account.setting.members;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.user.AvatarByUser;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class MemberListView extends Panel {

	IModel<List<Membership>> membershipsModel;
	IModel<User> accountModel;
	
	public MemberListView(String id, IModel<User> accountModel, IModel<List<User>> model) {
		super(id, model);
		
		this.setOutputMarkupId(true);
		
		this.accountModel = accountModel;
		membershipsModel = new LoadableDetachableModel<List<Membership>>() {

			@Override
			protected List<Membership> load() {
				Dao dao = Gitop.getInstance(Dao.class);
				EntityCriteria<Membership> criteria = EntityCriteria.of(Membership.class);
				criteria.createAlias("team", "team");
				criteria.add(Restrictions.eq("team.owner", getAccount()));
				return dao.query(criteria);
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();


		add(new Label("totals", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return ((List<?>) getDefaultModelObject()).size();
			}
			
		}));
		
		@SuppressWarnings("unchecked")
		ListView<User> membersView = new ListView<User>("member", (IModel<List<User>>) getDefaultModel()) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User user = item.getModelObject();
				item.add(new AvatarByUser("avatar", item.getModel()));
				AbstractLink link = PageSpec.newUserHomeLink("userLink", user);
				item.add(link);
				link.add(new Label("name", Model.of(user.getName())));
				if (Strings.isNullOrEmpty(user.getFullName())) {
					item.add(new Label("fullName", "&nbsp;").setEscapeModelStrings(false));
				} else {
					item.add(new Label("fullName", user.getFullName()));
				}
				item.add(createActionsPanel("actions", item.getModel()));
				item.add(createMemberTeams("teams", new UserModel(user)));
			}
			
		};
		
		add(membersView);
		
	}
	
	// retrieve memberships for user under current account
	private List<Membership> getMembershipsOfUser(User user) {
		List<Membership> memberships = membershipsModel.getObject();
		List<Membership> result = Lists.newArrayList();
		for (Membership each : memberships) {
			if (Objects.equal(each.getUser(), user)) {
				result.add(each);
			}
		}
		
		return result;
	}
	
	protected Component createMemberTeams(String id, final IModel<User> user) {
		final WebMarkupContainer teamsDiv = new WebMarkupContainer(id);
		teamsDiv.setOutputMarkupId(true);
		IModel<List<Membership>> teamsModel = new LoadableDetachableModel<List<Membership>>() {

			@Override
			protected List<Membership> load() {
				return getMembershipsOfUser(user.getObject());
			}
		};
		
		ListView<Membership> view = new ListView<Membership>("team", teamsModel) {

			@Override
			protected void populateItem(ListItem<Membership> item) {
				Membership membership = item.getModelObject();
				final Long membershipId = membership.getId();
				final int size = getList().size();
				
				WebMarkupContainer link;
				if (membership.getTeam().isOwners() && 
						Objects.equal(membership.getUser(), accountModel.getObject())) {
					// owner
					link = new WebMarkupContainer("removelink");
					link.add(AttributeAppender.append("class", "self disabled"));
				} else {
					link = new AjaxLink<Void>("removelink") {
	
								@Override
								public void onClick(AjaxRequestTarget target) {
									Dao dao = Gitop.getInstance(Dao.class);
									dao.remove(dao.load(Membership.class, membershipId));
									if (size > 1) {
										target.add(teamsDiv);
									} else {
										target.add(MemberListView.this);
									}
								}
					};
					link.add(new ConfirmBehavior("Are you sure you want to remove the user from team " + membership.getTeam().getName() + "?"));
				}
				
				link.add(new Label("teamName", Model.of(membership.getTeam().getName())));
				item.add(link);
			}
		};
		
		teamsDiv.add(view);
		return teamsDiv;
	}
	
	protected Component createActionsPanel(String id, IModel<User> model) {
		return new WebMarkupContainer(id).setVisibilityAllowed(false);
	}
	
	private User getAccount() {
		return accountModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		if (membershipsModel != null) {
			membershipsModel.detach();
		}
		
		if (accountModel != null) {
			accountModel.detach();
		}
		
		super.onDetach();
	}
}
