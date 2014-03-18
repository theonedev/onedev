package com.pmease.gitop.web.page.account.home;

import java.util.Date;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.ProjectHomeLink;
import com.pmease.gitop.web.model.RepositoryModel;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {

	public ProjectListPanel(String id, IModel<User> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<Project>> model = new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				User account = getThisAccount();
				List<Project> projects = Lists.newArrayList();
				for (Project each : account.getProjects()) {
					if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(each))) {
						projects.add(each);
					}
				}
				
				return projects;
			}
			
		};
		
		ListView<Project> projectsView = new ListView<Project>("project", model) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
//				IModel<Project> model = new ProjectModel(project);
				item.add(PageSpec.newProjectHomeLink("projectlink", project)
						.add(new Label("name", project.getName())));
				
				if (project.getForkedFrom() != null) {
					item.add(new ProjectHomeLink("forklink", new RepositoryModel(project.getForkedFrom())));
				} else {
					item.add(new WebMarkupContainer("forklink").setVisibilityAllowed(false));
				}
				
				item.add(new Label("description", project.getDescription()));
				
				final Long projectId = project.getId();
				item.add(new AgeLabel("lastUpdated", new AbstractReadOnlyModel<Date>() {

					@Override
					public Date getObject() {
						Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
						if (project.code().hasCommits()) {
							LogCommand command = new LogCommand(project.code().repoDir());
							List<Commit> commits = command.maxCount(1).call();
							Commit first = Iterables.getFirst(commits, null);
							return first.getCommitter().getDate();
						} else {
							return project.getCreatedAt();
						}
					}
				}));
			}
			
		};
		
		add(projectsView);
	}
	
	private User getThisAccount() {
		return (User) getDefaultModelObject();
	}
}
