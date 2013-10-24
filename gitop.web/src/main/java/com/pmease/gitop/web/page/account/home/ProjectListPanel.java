package com.pmease.gitop.web.page.account.home;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.component.link.ProjectHomeLink;
import com.pmease.gitop.web.model.ProjectModel;
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
					item.add(new ProjectHomeLink("forklink", new ProjectModel(project.getForkedFrom())));
				} else {
					item.add(new WebMarkupContainer("forklink").setVisibilityAllowed(false));
				}
				
				item.add(new Label("description", project.getDescription()));
			}
			
		};
		
		add(projectsView);
	}
	
	private User getThisAccount() {
		return (User) getDefaultModelObject();
	}
}
