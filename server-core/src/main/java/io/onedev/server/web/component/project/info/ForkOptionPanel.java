package io.onedev.server.web.component.project.info;

import static io.onedev.server.model.Project.PROP_CODE_MANAGEMENT_ENABLED;
import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT_ENABLED;
import static io.onedev.server.model.Project.PROP_NAME;

import java.util.Collection;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.setting.general.DefaultRoleBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;

@SuppressWarnings("serial")
abstract class ForkOptionPanel extends Panel {

	private final IModel<Project> projectModel;
	
	public ForkOptionPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = new Project();
		project.setForkedFrom(getProject());
		project.setName(getProject().getName());
		project.setIssueManagementEnabled(false);

		ParentBean parentBean = new ParentBean();
		
		String userName = SecurityUtils.getUser().getName();
		Project parent = getProjectManager().find(userName);
		if (parent != null) {
			if (SecurityUtils.canCreateChildren(parent))
				parentBean.setParent(parent);
		} else if (SecurityUtils.canCreateRootProjects()) {
			parent = new Project();
			parent.setName(userName);
			parent.setCodeManagementEnabled(false);
			parent.setIssueManagementEnabled(false);
			getProjectManager().create(parent);
			parentBean.setParent(parent);
		}
		
		DefaultRoleBean defaultRoleBean = new DefaultRoleBean();
		defaultRoleBean.setRole(getProject().getDefaultRole());
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_DESCRIPTION, 
				PROP_CODE_MANAGEMENT_ENABLED, PROP_ISSUE_MANAGEMENT_ENABLED);
		
		BeanEditor editor = BeanContext.edit("editor", project, properties, false);
		BeanEditor defaultRoleEditor = BeanContext.edit("defaultRoleEditor", defaultRoleBean);
		BeanEditor parentEditor = BeanContext.edit("parentEditor", parentBean);
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(editor);
		form.add(defaultRoleEditor);
		form.add(parentEditor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				Project parent = parentBean.getParent();
				if (parent != null && !SecurityUtils.canCreateChildren(parent) 
						|| parent == null && !SecurityUtils.canCreateRootProjects()) {
					throw new UnauthorizedException();
				}
				
				project.setParent(parent);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(parent, project.getName());
				if (projectWithSameName != null) {
					if (parent != null) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another child project");
					} else {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another root project");
					}
					target.add(form);
				} else {
					project.setDefaultRole(defaultRoleBean.getRole());
					projectManager.fork(getProject(), project);
					Session.get().success("Project forked");
					if (project.isCodeManagementEnabled())
						setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
					else if (project.isIssueManagementEnabled())
						setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project));
					else
						setResponsePage(ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(project));
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(form);
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
}
