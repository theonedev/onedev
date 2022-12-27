package io.onedev.server.web.component.project.forkoption;

import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
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
import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.setting.general.DefaultRoleBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;
import io.onedev.server.web.util.editablebean.LabelsBean;

@SuppressWarnings("serial")
public abstract class ForkOptionPanel extends Panel {

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
		
		Project editProject = new Project();
		editProject.setName(getProject().getName());
		editProject.setDescription(getProject().getDescription());

		ParentBean parentBean = new ParentBean();
		
		String userName = SecurityUtils.getUser().getName();
		Project parent = getProjectManager().findByPath(userName);
		if (parent != null) {
			if (SecurityUtils.canCreateChildren(parent))
				parentBean.setParentPath(parent.getPath());
		} else if (SecurityUtils.canCreateRootProjects()) {
			parentBean.setParentPath(userName);
		}
		
		DefaultRoleBean defaultRoleBean = new DefaultRoleBean();
		defaultRoleBean.setRole(getProject().getDefaultRole());
		
		LabelsBean labelsBean = LabelsBean.of(getProject());
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_DESCRIPTION, PROP_ISSUE_MANAGEMENT);
		
		BeanEditor editor = BeanContext.edit("editor", editProject, properties, false);
		BeanEditor defaultRoleEditor = BeanContext.edit("defaultRoleEditor", defaultRoleBean);
		BeanEditor labelsEditor = BeanContext.edit("labelsEditor", labelsBean);
		BeanEditor parentEditor = BeanContext.edit("parentEditor", parentBean);
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(editor);
		form.add(defaultRoleEditor);
		form.add(labelsEditor);
		form.add(parentEditor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				try {
					String projectPath = editProject.getName();
					if (parentBean.getParentPath() != null)
						projectPath = parentBean.getParentPath() + "/" + projectPath;
					Project newProject = getProjectManager().setup(projectPath);
					if (!newProject.isNew()) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another project");
						target.add(form);
					} else {
						newProject.setForkedFrom(getProject());
						newProject.setDescription(editProject.getDescription());
						newProject.setIssueManagement(editProject.isIssueManagement());
						newProject.setDefaultRole(defaultRoleBean.getRole());
						newProject.setCodeAnalysisSetting(getProject().getCodeAnalysisSetting());
						newProject.setGitPackConfig(getProject().getGitPackConfig());
						
						OneDev.getInstance(TransactionManager.class).run(new Runnable() {

							@Override
							public void run() {
								getProjectManager().create(newProject);
								getProjectManager().fork(getProject(), newProject);
								OneDev.getInstance(ProjectLabelManager.class).sync(newProject, labelsBean.getLabels());
							}
							
						});
						Session.get().success("Project forked");
						setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(newProject));
					}
				} catch (UnauthorizedException e) {
					parentEditor.error(new Path(new PathNode.Named("parentPath")), e.getMessage());
					target.add(form);
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
