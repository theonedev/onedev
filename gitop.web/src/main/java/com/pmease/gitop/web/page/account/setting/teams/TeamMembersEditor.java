package com.pmease.gitop.web.page.account.setting.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.component.choice.UserMultiChoice;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.members.MemberListView;

@SuppressWarnings("serial")
public class TeamMembersEditor extends Panel {

	public TeamMembersEditor(String id, IModel<Team> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newMembersForm());
		
		WebMarkupContainer membersDiv = new WebMarkupContainer("membersview");
		membersDiv.setOutputMarkupId(true);
		add(membersDiv);
		
		final IModel<List<User>> membersModel = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				Team team = getTeam();
				if (team.isNew()) {
					return Collections.emptyList();
				}

				List<User> users = Lists.newArrayList();
				if (team.isOwnersTeam()) {
					users.add(team.getOwner());
				}
				
				List<Membership> memberships = Gitop.getInstance(MembershipManager.class)
						.query(Restrictions.eq("team", team));
				for (Membership each : memberships) {
					users.add(each.getUser());
				}

				Collections.sort(users, new java.util.Comparator<User>() {

					@Override
					public int compare(User o1, User o2) {
						return o1.getName()
								.compareTo(o2.getName());
					}
				});

				return users;
			}
		};
		
		membersDiv.add(newMembersListView(membersModel));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Form<?> newMembersForm() {
		Form<?> form = new Form<Void>("form");
		form.add(new FeedbackPanel("feedback"));
		final IModel<Collection<User>> usersModel = new WildcardListModel(new ArrayList<User>());
		
		form.add(new UserMultiChoice("userchoice", usersModel));
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				form.error("Please fix errors below.");
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Set<User> users = Sets.newHashSet(usersModel.getObject());
				
				if (users.isEmpty()) {
					form.error("Please add an user first");
					target.add(form);
					return;
				}

				Team team = getTeam();
				for (Membership each : team.getMemberships()) {
					if (users.contains(each.getUser())) {
						users.remove(each.getUser());
					}
				}
				
				MembershipManager mm = Gitop
						.getInstance(MembershipManager.class);
				
				for (User each : users) {
					Membership m = new Membership();
					m.setTeam(team);
					m.setUser(each);
					mm.save(m);
				}
				
				usersModel.setObject(new ArrayList<User>());
				target.add(form);
				onMembersChanged(target);
			}
		});

		return form;
	}

	private Component newMembersListView(IModel<List<User>> membersModel) {
		return new MemberListView("members", new UserModel(getTeam().getOwner()), membersModel){
			@Override
			protected Component createActionsPanel(String id, IModel<User> model) {
				Fragment frag = new Fragment(id, "memberactionfrag", TeamMembersEditor.this);
				final IModel<User> userModel = new UserModel(model.getObject());
				frag.add(new AjaxLink<Void>("remove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						MembershipManager mm = Gitop.getInstance(MembershipManager.class);
						User user = userModel.getObject();
						Team team = getTeam();
						Membership membership = mm.find(
								Restrictions.eq("user", user),
								Restrictions.eq("team", team));
						if (membership != null) {
							Gitop.getInstance(MembershipManager.class).delete(membership);
							Messenger.warn(String.format("User [%s] is removed from team [%s]", 
									user.getName(), 
									team.getName()))
									.run(target);
							
							onMembersChanged(target);
						}
					}
				}.setVisibilityAllowed(!Objects.equal(getTeam().getOwner(), model.getObject())));
				return frag;
			}
		}.setOutputMarkupId(true);
	}
	
	private void onMembersChanged(AjaxRequestTarget target) {
		target.add(get("membersview"));
	}
	
	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}
}
