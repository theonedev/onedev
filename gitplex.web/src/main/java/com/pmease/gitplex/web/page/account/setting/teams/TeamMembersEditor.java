package com.pmease.gitplex.web.page.account.setting.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.util.time.Duration;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserMultiChoice;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.setting.members.MemberListView;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class TeamMembersEditor extends Panel {

	public TeamMembersEditor(String id, IModel<Team> model) {
		super(id, model);
	}

	private Form<?> membersForm;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		this.membersForm = newMembersForm();
		add(membersForm);
		
		WebMarkupContainer membersDiv = new WebMarkupContainer("membersview");
		membersDiv.setOutputMarkupId(true);
		membersDiv.add(new TooltipBehavior(new TooltipConfig().withSelector("a.remove-link")));
		add(membersDiv);
		
		final IModel<List<User>> membersModel = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				Team team = getTeam();
				if (team.getId() == null) {
					return Collections.emptyList();
				}

				List<User> users = Lists.newArrayList();
				List<Membership> memberships = GitPlex.getInstance(Dao.class)
						.query(EntityCriteria.of(Membership.class).add(Restrictions.eq("team", team)));
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
		
		Component membersView = newMembersListView(membersModel); 
		membersDiv.add(membersView);
		FeedbackPanel removeMembersFeedback = new FeedbackPanel("removeMemberFeedback", membersDiv);
		membersDiv.add(removeMembersFeedback);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Form<?> newMembersForm() {
		Form<?> form = new Form<Void>("form");
		form.add(new FeedbackPanel("feedback", form).hideAfter(Duration.seconds(10)));
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
					form.warn("Please add an user first");
					target.add(form);
					return;
				}

				Team team = getTeam();
				Dao dao = GitPlex.getInstance(Dao.class);
				List<Membership> memberships = dao.query(EntityCriteria.of(Membership.class).add(Restrictions.eq("team", team)));
				for (Membership each : memberships) {
					if (users.contains(each.getUser())) {
						users.remove(each.getUser());
					}
				}
				
				for (User each : users) {
					Membership m = new Membership();
					m.setTeam(team);
					m.setUser(each);
					dao.persist(m);
				}
				
				usersModel.setObject(new ArrayList<User>());
				if (users.size() == 0) {
					form.warn("Users were added before");
					target.add(form);
				} else {
					form.success(users.size() + " new members added to this team");
					target.add(form);
					onMembersChanged(target);
				}
			}
		});

		return form;
	}

	private Component newMembersListView(IModel<List<User>> membersModel) {
		return new MemberListView("members", new UserModel(getTeam().getOwner()), membersModel){
			
			@Override
			protected Component createMemberTeams(String id, final IModel<User> user) {
				return new WebMarkupContainer(id).setVisibilityAllowed(false);
			}
			
			@Override
			protected Component createActionsPanel(String id, IModel<User> model) {
				Fragment frag = new Fragment(id, "memberactionfrag", TeamMembersEditor.this);
				frag.add(new AjaxLink<User>("remove", new UserModel(model.getObject())) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Dao dao = GitPlex.getInstance(Dao.class);
						User user = (User) getDefaultModelObject();
						Team team = getTeam();
						Membership membership = dao.find(EntityCriteria.of(Membership.class)
								.add(Restrictions.eq("user", user))
								.add(Restrictions.eq("team", team)));
						if (membership != null) {
							dao.remove(membership);
							
							// Use membersForm to show feedback?
							//
							membersForm.info(String.format("User %s is removed from the team", 
									user.getName()));
							target.add(membersForm);
							onMembersChanged(target);
						}
					}
				}.setVisibilityAllowed(
						!(getTeam().isOwners() && Objects.equal(getTeam().getOwner(), model.getObject()))));
				return frag;
			}
		}.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
	private void onMembersChanged(AjaxRequestTarget target) {
		target.add(get("membersview"));
//		target.appendJavaScript(JQuery.$("a.remove-link").chain("tooltip").get());
	}
	
	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}
}
