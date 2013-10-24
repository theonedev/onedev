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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.component.choice.MultipleUserChoice;
import com.pmease.gitop.web.component.members.MemberListView;
import com.pmease.gitop.web.model.UserModel;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

	private final IModel<User> userModel;

	private WebMarkupContainer membersContainer;
	
	public TeamEditor(String id, IModel<User> userModel, IModel<Team> teamModel) {
		super(id, teamModel);
		this.userModel = userModel;
		this.setOutputMarkupId(true);
	}

	private User getAccount() {
		return userModel.getObject();
	}

	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		this.name = getTeam().getName();
		this.currentPermission = getTeam().getAuthorizedOperation();
		
		add(createInfoForm());
		add(createPermissionContainer());
		membersContainer = new WebMarkupContainer("span") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(!getTeam().isNew());
			}
		};
		add(membersContainer);
		membersContainer.setOutputMarkupId(true);
		
		membersContainer.add(createMembersForm());

		final IModel<List<User>> membersModel = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				Team team = getTeam();
				if (team.isNew()) {
					return Collections.emptyList();
				}

				List<User> users = Lists.newArrayList();
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

//		membersContainer.add(createMemberList("oddlist", relationModel));
//		membersContainer.add(createMemberList("evenlist", relationModel, false));
		membersContainer.add(new Label("total", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return membersModel.getObject().size();
			}
		}).setOutputMarkupId(true));
		
		membersContainer.add(new MemberListView("members", membersModel){
			@Override
			protected Component createActionsPanel(String id, IModel<User> model) {
				Fragment frag = new Fragment(id, "memberactionfrag", TeamEditor.this);
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
				});
				return frag;
			}
		}.setOutputMarkupId(true));
	}

	private GeneralOperation currentPermission;
	private String name;
	
	private Form<?> createInfoForm() {
		Form<?> infoForm = new Form<Void>("infoForm");
		add(infoForm);
		infoForm.add(new FeedbackPanel("feedback"));
		infoForm.add(new TextField<String>("name", new PropertyModel<String>(
				this, "name")).add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				TeamManager tm = Gitop.getInstance(TeamManager.class);
				Team team = tm.find(getAccount(), validatable.getValue());
				Team current = getTeam();
				if (team != null && !Objects.equal(team, current)) {
					validatable.error(new ValidationError()
							.setMessage("The team is already exist."));
				}
			}
		}).setRequired(true));
		
		AjaxButton btn = new AjaxButton("submit", infoForm) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Team team = getTeam();
				boolean isNew = team.isNew();
				User owner = getAccount();
				Preconditions.checkNotNull(owner, "owner");

				team.setName(name);
				team.setAuthorizedOperation(currentPermission);
				team.setOwner(owner);

				Gitop.getInstance(TeamManager.class).save(team);
				target.add(TeamEditor.this);
				
				Messenger.success(
						String.format("Team has been %s successfully.",
								isNew ? "created" : "updated")).run(target);
			}
		};

		btn.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getTeam().isNew() ? "Create Team" : "Update Team";
			}
			
		}));
		
		infoForm.add(btn);
		
		return infoForm;
	}

	class PermissionLink extends AjaxLink<GeneralOperation> {
		
		PermissionLink(String id, IModel<GeneralOperation> permission) {
			super(id, permission);
			add(AttributeAppender.append("class",
					new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return Objects.equal(currentPermission, getPermission()) ? 
									"active" : "";
						}
					}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (Objects.equal(currentPermission, getPermission())) {
				return;
			}

			currentPermission = getPermission();
			Team team = getTeam();
			team.setAuthorizedOperation(currentPermission);
			if (!team.isNew()) {
				Gitop.getInstance(TeamManager.class).save(team);
			}
			
			target.add(TeamEditor.this.get("permissionContainer"));
		}
		
		GeneralOperation getPermission() {
			return (GeneralOperation) getDefaultModelObject();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Form<?> createMembersForm() {
		Form<?> form = new Form<Void>("membersForm");
		form.add(new FeedbackPanel("feedback"));
		final IModel<Collection<User>> usersModel = new WildcardListModel(new ArrayList<User>());
		
		form.add(new MultipleUserChoice("userchoice", usersModel));

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

	private Component createPermissionContainer() {
		WebMarkupContainer permissionContainer = new WebMarkupContainer("permissionContainer");
		permissionContainer.setOutputMarkupId(true);
		permissionContainer.add(new ListView<GeneralOperation>("permissions",
				ImmutableList.<GeneralOperation>copyOf(GeneralOperation.values())) {

			@Override
			protected void populateItem(ListItem<GeneralOperation> item) {
				GeneralOperation permission = item.getModelObject();
				AjaxLink<?> link = new PermissionLink("permission", Model.of(permission));
				link.add(new Label("name", permission.toString()));
				item.add(link);
			}

		});
		return permissionContainer;
	}
	
	private void onMembersChanged(AjaxRequestTarget target) {
		target.add(membersContainer.get("members"));
		target.add(membersContainer.get("total"));
	}

	@Override
	public void onDetach() {
		if (userModel != null) {
			userModel.detach();
		}

		super.onDetach();
	}
}
