package com.pmease.gitplex.web.page.account.setting.teams;

import java.util.List;

import com.pmease.gitplex.core.GitPlex;

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
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

	public TeamEditor(String id, IModel<Team> teamModel) {
		super(id, teamModel);
		this.setOutputMarkupId(true);
	}

	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}

	private WebMarkupContainer membersDiv;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<Team> teamModel = (IModel<Team>) getDefaultModel();
		add(new TeamPropForm("infoForm", teamModel).setVisibilityAllowed(!getTeam().isOwners()));
		membersDiv = new WebMarkupContainer("members") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Team team = getTeam();
				setVisibilityAllowed(team.getId() != null && !team.isAnonymous() && !team.isLoggedIn());
			}
		};
		
		membersDiv.setOutputMarkupId(true);
		add(membersDiv);
		
		
		membersDiv.add(new TeamMembersEditor("teammembers", teamModel));
//		moreDiv.add(new TeamRepositoriesEditor("teamrepositories", teamModel));
	}

	private class TeamPropForm extends Form<Team> {

		private String oldTeamName;
		
		public TeamPropForm(String id, IModel<Team> model) {
			super(id, model);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			Team team = getTeam();
			oldTeamName = team.getName();
			add(new FeedbackPanel("feedback", this));
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
							
							if (getTeam().getId() != null && name.equalsIgnoreCase(oldTeamName)) {
								return; // name not change
							}
							
							Dao dao = GitPlex.getInstance(Dao.class);
							boolean b = dao.find(EntityCriteria.of(Team.class)
									.add(Restrictions.eq("owner", getTeam().getOwner()))
									.add(Restrictions.eq("name", name).ignoreCase())) != null;
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
					boolean isNew = team.getId() == null;
					GitPlex.getInstance(Dao.class).persist(team);
					if (isNew) {
						setResponsePage(EditTeamPage.class, EditTeamPage.newParams(team));
					} else {
						form.success(
								String.format("Team has been updated successfully."));
						target.add(form);
					}
					
				}
			});
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}
