package com.pmease.gitop.web.page.account.setting.teams;

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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

	private final IModel<User> userModel;
	private GeneralOperation currentPermission;
	private String name;
	
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

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		this.name = getTeam().getName();
		this.currentPermission = getTeam().getAuthorizedOperation();
		
		add(createInfoForm());
		
		WebMarkupContainer moreDiv = new WebMarkupContainer("more") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(!getTeam().isNew());
			}
		};
		moreDiv.setOutputMarkupId(true);
		add(moreDiv);
		
		IModel<Team> teamModel = (IModel<Team>) getDefaultModel();
		moreDiv.add(new TeamMembersEditor("teammembers", teamModel));
		moreDiv.add(new TeamProjectsEditor("teamprojects", teamModel));
	}

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
		
		infoForm.add(createPermissionContainer());
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
			
			target.add(TeamEditor.this.get("infoForm:permissionContainer"));
		}
		
		GeneralOperation getPermission() {
			return (GeneralOperation) getDefaultModelObject();
		}
	}

	private Component createPermissionContainer() {
		WebMarkupContainer permissionContainer = new WebMarkupContainer("permissionContainer");
		permissionContainer.setOutputMarkupId(true);
		permissionContainer.add(new ListView<GeneralOperation>("permissions",
				ImmutableList.<GeneralOperation>of(GeneralOperation.READ, GeneralOperation.WRITE, GeneralOperation.ADMIN)) {

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
	
	@Override
	public void onDetach() {
		if (userModel != null) {
			userModel.detach();
		}

		super.onDetach();
	}
}
