package com.pmease.gitop.web.page.account.setting.permission;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.component.choice.SingleUserChoice;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

	private final IModel<User> userModel;

	private WebMarkupContainer membersContainer;
	
	public TeamEditor(String id, IModel<User> userModel, IModel<Team> model) {
		super(id, model);
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
		this.operation = getTeam().getAuthorizedOperation();
		
		add(createInfoForm());
		
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

		final IModel<List<Membership>> relationModel = new LoadableDetachableModel<List<Membership>>() {

			@Override
			protected List<Membership> load() {
				Team team = getTeam();
				if (team.isNew()) {
					return Collections.emptyList();
				}

				List<Membership> r = Gitop.getInstance(MembershipManager.class)
						.query(Restrictions.eq("team", getTeam()));

				Collections.sort(r, new java.util.Comparator<Membership>() {

					@Override
					public int compare(Membership o1, Membership o2) {
						return o1.getUser().getName()
								.compareTo(o2.getUser().getName());
					}
				});

				return r;
			}
		};

		membersContainer.add(createMemberList("oddlist", relationModel, true));
		membersContainer.add(createMemberList("evenlist", relationModel, false));
		membersContainer.add(new Label("total", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return relationModel.getObject().size();
			}
		}).setOutputMarkupId(true));
	}

	private GeneralOperation operation;
	private String name;
	
	private Form<?> createInfoForm() {
		operation = getTeam().getAuthorizedOperation();
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
		WebMarkupContainer permissionContainer = new WebMarkupContainer(
				"permissionContainer");
		permissionContainer.setOutputMarkupId(true);
		infoForm.add(permissionContainer);
		permissionContainer.add(new ListView<GeneralOperation>("permissions",
				ImmutableList.<GeneralOperation> of(
						GeneralOperation.READ,
						GeneralOperation.WRITE, 
						GeneralOperation.ADMIN)) {

			@Override
			protected void populateItem(ListItem<GeneralOperation> item) {
				GeneralOperation permission = item.getModelObject();
				AjaxLink<?> link = new PermissionLink("permission", permission);
				link.add(new Label("name", permission.toString()));
				item.add(link);
			}

		});

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
				team.setAuthorizedOperation(operation);
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

	class PermissionLink extends AjaxLink<Void> {
		final GeneralOperation permission;

		PermissionLink(String id, final GeneralOperation permission) {
			super(id);
			this.permission = permission;

			add(AttributeAppender.append("class",
					new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return Objects.equal(operation, permission) ? 
									"active" : "";
						}
					}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			if (Objects.equal(operation, permission)) {
				return;
			}

			operation = permission;
			target.add(TeamEditor.this.get("infoForm").get("permissionContainer"));
		}
	}

	User userToAdd;

	private Form<?> createMembersForm() {
		Form<?> form = new Form<Void>("membersForm");
		form.add(new FeedbackPanel("feedback"));
		form.add(new SingleUserChoice("userchoice", new PropertyModel<User>(
				this, "userToAdd")));

		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				form.error("Please fix errors below.");
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (userToAdd == null) {
					form.error("Please select an user first");
					target.add(form);
					return;
				}

				Team team = getTeam();
				MembershipManager mm = Gitop
						.getInstance(MembershipManager.class);
				Membership m = mm.find(Restrictions.eq("team", team),
						Restrictions.eq("user", userToAdd));
				if (m != null) {
					form.warn("User has been added already");
					target.add(form);
					return;
				}

				m = new Membership();
				m.setUser(userToAdd);
				m.setTeam(team);
				mm.save(m);

				Messenger.success(String.format("User [%s] is added to team [%s]", 
									userToAdd.getName(), team.getName()))
						.run(target);
				userToAdd = null;
				target.add(form);
				onMembersChanged(target);
			}
		});

		return form;
	}

	private void onMembersChanged(AjaxRequestTarget target) {
		target.add(membersContainer.get("oddlist"));
		target.add(membersContainer.get("evenlist"));
		target.add(membersContainer.get("total"));
	}

	private Component createMemberList(String id,
			IModel<List<Membership>> model, final boolean expected) {
		Fragment frag = new Fragment(id, "membersview", this);
		frag.setOutputMarkupId(true);
		frag.add(new ListView<Membership>("member", model) {

			@Override
			protected void populateItem(ListItem<Membership> item) {
				int index = item.getIndex();
				boolean odd = index % 2 == 0;
				if (odd != expected) {
					item.setVisibilityAllowed(false);
					return;
				}

				Membership membership = item.getModelObject();
				User user = membership.getUser();
				item.add(new AvatarImage("avatar", new UserModel(user)));
				Link<?> link = PageSpec.newUserHomeLink("link", user);
				link.add(new Label("name", user.getName()));
				link.add(new Label("fullname", user.getDisplayName()));
				item.add(link);

				final Long id = membership.getId();
				item.add(new AjaxLink<Void>("remove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						MembershipManager mm = Gitop.getInstance(MembershipManager.class);
						Membership membership = mm.get(id);
						Gitop.getInstance(MembershipManager.class).delete(membership);
						Messenger.warn(String.format("User [%s] is removed from team [%s]", 
								membership.getUser().getName(), 
								membership.getTeam().getName()))
								.run(target);
						
						onMembersChanged(target);
					}
				});
			}
		});

		return frag;
	}

	@Override
	public void onDetach() {
		if (userModel != null) {
			userModel.detach();
		}

		super.onDetach();
	}
}
