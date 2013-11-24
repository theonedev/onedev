package com.pmease.gitop.web.page.account.setting.teams;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

//	private GeneralOperation currentPermission;
//	private String name;
	
	public TeamEditor(String id, IModel<Team> teamModel) {
		super(id, teamModel);
//		this.userModel = userModel;
		this.setOutputMarkupId(true);
	}

//	private User getAccount() {
//		return userModel.getObject();
//	}

	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}

	private WebMarkupContainer membersDiv;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

//		this.name = getTeam().getName();
//		this.currentPermission = getTeam().getDefaultRepoPermission();
		
//		add(createInfoForm());
		
		IModel<Team> teamModel = (IModel<Team>) getDefaultModel();
		add(new TeamPropForm("infoForm", teamModel).setVisibilityAllowed(!getTeam().isOwners()));
		membersDiv = new WebMarkupContainer("members") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Team team = getTeam();
				setVisibilityAllowed(!team.isNew() && !team.isAnonymous() && !team.isLoggedIn());
			}
		};
		
		membersDiv.setOutputMarkupId(true);
		add(membersDiv);
		
		
		membersDiv.add(new TeamMembersEditor("teammembers", teamModel));
//		moreDiv.add(new TeamProjectsEditor("teamprojects", teamModel));
	}

	private class TeamPropForm extends Form<Team> {

		public TeamPropForm(String id, IModel<Team> model) {
			super(id, model);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			Team team = getTeam();
			final String oldName = team.getName();
			add(new FeedbackPanel("feedback"));
			add(new TextField<String>("name", new PropertyModel<String>(getDefaultModel(), "name"))
					.add(new IValidator<String>() {

						@Override
						public void validate(IValidatable<String> validatable) {
							String name = validatable.getValue();
							
							if (Team.ANONYMOUS.equalsIgnoreCase(name)
									|| Team.LOGGEDIN.equalsIgnoreCase(name)
									|| Team.OWNERS.equalsIgnoreCase(name)) {
								validatable.error(new ValidationError().setMessage("The name is already exist"));
								return;
							}
							
							if (!getTeam().isNew() && name.equalsIgnoreCase(oldName)) {
								return; // name not change
							}
							
							TeamManager tm = Gitop.getInstance(TeamManager.class);
							boolean b = tm.find(Restrictions.eq("owner", getTeam().getOwner()),
									Restrictions.eq("name", name).ignoreCase()) != null;
							if (b) {
								validatable.error(new ValidationError().setMessage("The name is already exist"));
								return;
							}
						}
						
					})
					.add(new PropertyValidator<String>())
					.setRequired(true)
					.setEnabled(!team.isBuiltIn()));
			
			IModel<List<? extends GeneralOperation>> listModel = new AbstractReadOnlyModel<List<? extends GeneralOperation>>() {

						@Override
						public List<GeneralOperation> getObject() {
							Team team = getTeam();
							if (team.isAnonymous()) {
								return ImmutableList.<GeneralOperation>of(
										GeneralOperation.NO_ACCESS,
										GeneralOperation.READ);
							} else if (team.isLoggedIn()){
								return ImmutableList.<GeneralOperation>of(
										GeneralOperation.NO_ACCESS,
										GeneralOperation.READ,
										GeneralOperation.WRITE);
							} else if (team.isOwners()) {
								return ImmutableList.of(GeneralOperation.ADMIN);
							} else {
								return ImmutableList.<GeneralOperation>copyOf(
										GeneralOperation.values());
							}
						}
				
			};
			
			add(new DropDownChoice<GeneralOperation>("permission",
					new PropertyModel<GeneralOperation>(getDefaultModel(), "authorizedOperation"),
					listModel));
			
			add(new AjaxButton("submit", this) {
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(form);
				}
				
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					Team team = getTeam();
					boolean isNew = team.isNew();
					Gitop.getInstance(TeamManager.class).save(team);
					target.add(TeamEditor.this);
					Messenger.success(
							String.format("Team has been %s successfully.",
									isNew ? "created" : "updated")).run(target);
				}
			});
		}
	}
	
//	private Form<?> createInfoForm() {
//		Form<?> infoForm = new Form<Void>("infoForm");
//		add(infoForm);
//		infoForm.add(new FeedbackPanel("feedback"));
//		infoForm.add(new TextField<String>("name", new PropertyModel<String>(
//				this, "name")).add(new IValidator<String>() {
//
//			@Override
//			public void validate(IValidatable<String> validatable) {
//				TeamManager tm = Gitop.getInstance(TeamManager.class);
//				Team team = tm.find(getAccount(), validatable.getValue());
//				Team current = getTeam();
//				if (team != null && !Objects.equal(team, current)) {
//					validatable.error(new ValidationError()
//							.setMessage("The team is already exist."));
//				}
//			}
//		}).setRequired(true));
//		
//		infoForm.add(createPermissionContainer());
//		AjaxButton btn = new AjaxButton("submit", infoForm) {
//			@Override
//			protected void onError(AjaxRequestTarget target, Form<?> form) {
//				target.add(form);
//			}
//
//			@Override
//			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
//				Team team = getTeam();
//				boolean isNew = team.isNew();
//				User owner = getAccount();
//				Preconditions.checkNotNull(owner, "owner");
//
//				team.setName(name);
//				team.setAuthorizedOperation(currentPermission);
//				team.setOwner(owner);
//
//				Gitop.getInstance(TeamManager.class).save(team);
//				target.add(TeamEditor.this);
//				
//				Messenger.success(
//						String.format("Team has been %s successfully.",
//								isNew ? "created" : "updated")).run(target);
//			}
//		};
//
//		btn.add(new Label("label", new AbstractReadOnlyModel<String>() {
//
//			@Override
//			public String getObject() {
//				return getTeam().isNew() ? "Create Team" : "Update Team";
//			}
//			
//		}));
//		
//		infoForm.add(btn);
//		
//		return infoForm;
//	}
//
//	class PermissionLink extends AjaxLink<GeneralOperation> {
//		
//		PermissionLink(String id, IModel<GeneralOperation> permission) {
//			super(id, permission);
//			add(AttributeAppender.append("class",
//					new AbstractReadOnlyModel<String>() {
//
//						@Override
//						public String getObject() {
//							return Objects.equal(currentPermission, getPermission()) ? 
//									"active" : "";
//						}
//					}));
//		}
//
//		@Override
//		public void onClick(AjaxRequestTarget target) {
//			if (Objects.equal(currentPermission, getPermission())) {
//				return;
//			}
//
//			currentPermission = getPermission();
//			Team team = getTeam();
//			team.setAuthorizedOperation(currentPermission);
//			if (!team.isNew()) {
//				Gitop.getInstance(TeamManager.class).save(team);
//			}
//			
//			target.add(TeamEditor.this.get("infoForm:permissionContainer"));
//		}
//		
//		GeneralOperation getPermission() {
//			return (GeneralOperation) getDefaultModelObject();
//		}
//	}
//
//	private Component createPermissionContainer() {
//		WebMarkupContainer permissionContainer = new WebMarkupContainer("permissionContainer");
//		permissionContainer.setOutputMarkupId(true);
//		permissionContainer.add(new ListView<GeneralOperation>("permissions",
//				ImmutableList.<GeneralOperation>of(GeneralOperation.READ, GeneralOperation.WRITE, GeneralOperation.ADMIN)) {
//
//			@Override
//			protected void populateItem(ListItem<GeneralOperation> item) {
//				GeneralOperation permission = item.getModelObject();
//				AjaxLink<?> link = new PermissionLink("permission", Model.of(permission));
//				link.add(new Label("name", permission.toString()));
//				item.add(link);
//			}
//
//		});
//		return permissionContainer;
//	}
//	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}
