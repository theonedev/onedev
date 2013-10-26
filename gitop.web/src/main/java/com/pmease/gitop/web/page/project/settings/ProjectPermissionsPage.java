package com.pmease.gitop.web.page.project.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Authorization;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.component.choice.TeamChoiceProvider;
import com.pmease.gitop.web.component.choice.TeamMultiChoice;
import com.pmease.gitop.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitop.web.page.project.ProjectPubliclyAccessibleChanged;

@SuppressWarnings("serial")
public class ProjectPermissionsPage extends AbstractProjectSettingPage {

	public ProjectPermissionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PERMISSIONS;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(newPublicSection());
		add(newLoggedInSection());
		add(newTeamsForm());
		add(newTeamsView());
	}
	

	private Component newPublicSection() {
		AbstractLink link = new AjaxLink<Void>("publiclink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Project project = getProject();
				project.setPubliclyAccessible(!project.isPubliclyAccessible());
				if (project.isPubliclyAccessible()) {
					GeneralOperation defaultPermission = project.getDefaultAuthorizedOperation();
					if (defaultPermission == GeneralOperation.NO_ACCESS) {
						project.setDefaultAuthorizedOperation(GeneralOperation.READ);
					}
				}
				
				Gitop.getInstance(ProjectManager.class).save(project);
				target.add(this);
				target.add(ProjectPermissionsPage.this.get("loggedInPermissions"));
				
				send(getPage(), Broadcast.DEPTH, new ProjectPubliclyAccessibleChanged(target));
			}
		};
		
		link.setOutputMarkupId(true);
		link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Project project = getProject();
				if (project.isPubliclyAccessible()) {
					return "checked";
				} else {
					return "";
				}
			}
			
		}));
		
		return link;
	}
	
	private Component newLoggedInSection() {
		WebMarkupContainer loggedInPanel = new WebMarkupContainer("loggedInPermissions");
		loggedInPanel.setOutputMarkupId(true);
		IModel<List<GeneralOperation>> permissionsModel = new AbstractReadOnlyModel<List<GeneralOperation>>() {

			@Override
			public List<GeneralOperation> getObject() {
				if (getProject().isPubliclyAccessible()) {
					return ImmutableList.<GeneralOperation>of(GeneralOperation.READ, GeneralOperation.WRITE);
				} else {
					return ImmutableList.<GeneralOperation>of(GeneralOperation.NO_ACCESS, GeneralOperation.READ, GeneralOperation.WRITE);
				}
			}
		};
		
		loggedInPanel.add(new ListView<GeneralOperation>("permissions", permissionsModel) {

					@Override
					protected void populateItem(ListItem<GeneralOperation> item) {
						AjaxLink<?> link = new PermissionLink("permission", item.getModelObject());
						item.add(link);
						link.add(new Label("name", item.getModelObject().toString()));
					}
			
		});
		
		return loggedInPanel;
	}
	
	class PermissionLink extends AjaxLink<Void> {
		final GeneralOperation permission;
		
		PermissionLink(String id, final GeneralOperation permission) {
			super(id);
			this.permission = permission;
			
			add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return Objects.equal(getProject().getDefaultAuthorizedOperation(), permission) ? "btn-default active" : "btn-default";
				}
			}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			Project project = getProject();
			project.setDefaultAuthorizedOperation(permission);
			Gitop.getInstance(ProjectManager.class).save(project);
			
			target.add(ProjectPermissionsPage.this.get("loggedInPermissions"));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Component newTeamsForm() {
		Form<?> form = new Form<Void>("form");
		form.add(new FeedbackPanel("feedback", form));
		final IModel<Collection<Team>> teamsModel = new WildcardListModel(new ArrayList<Team>());
		form.add(new TeamMultiChoice("teamchoice", teamsModel, 
				new TeamChoiceProvider(new AbstractReadOnlyModel<DetachedCriteria>() {

			@Override
			public DetachedCriteria getObject() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Team.class);
				criteria.add(Restrictions.eq("owner", getAccount()));
				criteria.addOrder(Order.asc("name"));
				return criteria;
			}
			
		})));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Collection<Team> teams = teamsModel.getObject();
				Project project = getProject();
				AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
				for (Team team : teams) {
					if (am.find(Restrictions.eq("team", team), Restrictions.eq("project", project)) == null) {
						Authorization authorization = new Authorization();
						authorization.setProject(project);
						authorization.setTeam(team);
						am.save(authorization);
						
						Messenger.success("Team - " + team.getName() + " is granted to access project " + project).run(target);
					}
				}
				
				teamsModel.setObject(new ArrayList<Team>());
				target.add(form);
				onTeamsChanged(target);
			}
		});
		
		return form;
	}
	
	private void onTeamsChanged(AjaxRequestTarget target) {
		target.add(get("teams"));
	}
	
	private Component newTeamsView() {
		WebMarkupContainer teamsDiv = new WebMarkupContainer("teams");
		teamsDiv.setOutputMarkupId(true);
		
		IModel<List<Authorization>> model = new LoadableDetachableModel<List<Authorization>>() {

			@Override
			protected List<Authorization> load() {
				AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
				return am.query(
						new Criterion[] {Restrictions.eq("project", getProject())},
						new Order[] { Order.desc("id") }, 
						0, Integer.MAX_VALUE);
			}
			
		};
		
		ListView<Authorization> view = new ListView<Authorization>("team", model) {

			@Override
			protected void populateItem(ListItem<Authorization> item) {
				Authorization a = item.getModelObject();
				Team team = a.getTeam();
				
				AbstractLink link = new BookmarkablePageLink<Void>("editlink", EditTeamPage.class, EditTeamPage.newParams(team));
				link.setEnabled(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount())));
				item.add(link);
				link.add(new Label("name", Model.of(team.getName())));
				item.add(new Label("members", Model.of(team.getMemberships().size())));
				item.add(new Label("permission", team.getAuthorizedOperation().name()));
				
				final Long id = a.getId();
				item.add(new AjaxConfirmLink<Void>("removelink",
						Model.of("Removing this team means all members "
								+ "in this team may not be able to access this project, are you sure?")) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						AuthorizationManager am = Gitop.getInstance(AuthorizationManager.class);
						am.delete(am.get(id));
						onTeamsChanged(target);
					}
				});
			}
		};
		
		teamsDiv.add(view);
		
		return teamsDiv;
	}
}
