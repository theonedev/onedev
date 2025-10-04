package io.onedev.server.web.component.project.forkoption;

import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_KEY;
import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_PACK_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_TIME_TRACKING;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

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
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.setting.general.DefaultRolesBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;
import io.onedev.server.web.util.editbean.LabelsBean;

public abstract class ForkOptionPanel extends Panel {

	private final IModel<Project> projectModel;
	
	public ForkOptionPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project editProject = new Project();
		editProject.setName(getProject().getName());
		editProject.setDescription(getProject().getDescription());

		ParentBean parentBean = new ParentBean();
		
		String userName = SecurityUtils.getAuthUser().getName();
		Project parent = getProjectService().findByPath(userName);
		if (parent != null) {
			if (SecurityUtils.canCreateChildren(parent))
				parentBean.setParentPath(parent.getPath());
		} else if (SecurityUtils.canCreateRootProjects()) {
			parentBean.setParentPath(userName);
		}
		
		DefaultRolesBean defaultRolesBean = new DefaultRolesBean();
		defaultRolesBean.setRoles(getProject().getBaseAuthorizations().stream().map(it->it.getRole()).collect(toList()));
		
		LabelsBean labelsBean = LabelsBean.of(getProject());
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_KEY, PROP_DESCRIPTION, 
				PROP_PACK_MANAGEMENT, PROP_ISSUE_MANAGEMENT, PROP_TIME_TRACKING);
		
		BeanEditor editor = BeanContext.edit("editor", editProject, properties, false);
		BeanEditor defaultRoleEditor = BeanContext.edit("defaultRoleEditor", defaultRolesBean);
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
					if (editProject.getKey() != null && getProjectService().findByKey(editProject.getKey()) != null) {
						editor.error(new Path(new PathNode.Named(PROP_KEY)),
								_T("This key has already been used by another project"));
					}
					String projectPath = editProject.getName();
					if (parentBean.getParentPath() != null)
						projectPath = parentBean.getParentPath() + "/" + projectPath;
					var subject = SecurityUtils.getSubject();
					Project newProject = getProjectService().setup(subject, projectPath);
					if (!newProject.isNew()) {
						editor.error(new Path(new PathNode.Named("name")),
								_T("This name has already been used by another project"));
					} 
					if (editor.isValid()) {
						newProject.setForkedFrom(getProject());
						newProject.setKey(editProject.getKey());
						newProject.setDescription(editProject.getDescription());
						newProject.setPackManagement(editProject.isPackManagement());
						newProject.setIssueManagement(editProject.isIssueManagement());
						newProject.setTimeTracking(editProject.isTimeTracking());
						newProject.setCodeAnalysisSetting(getProject().getCodeAnalysisSetting());
						newProject.setGitPackConfig(getProject().getGitPackConfig());
						
						OneDev.getInstance(TransactionService.class).run(() -> {
							getProjectService().create(SecurityUtils.getUser(subject), newProject);
							getProjectService().fork(getProject(), newProject);							
							OneDev.getInstance(BaseAuthorizationService.class).syncRoles(newProject, defaultRolesBean.getRoles());
							OneDev.getInstance(ProjectLabelService.class).sync(newProject, labelsBean.getLabels());

							var auditData = editor.getPropertyValues();
							auditData.put("parent", parentBean.getParentPath());
							auditData.put("forkedFrom", getProject().getPath());
							auditData.put("defaultRoles", defaultRolesBean.getRoleNames());
							auditData.put("labels", labelsBean.getLabels());
							OneDev.getInstance(AuditService.class).audit(newProject, "created project", null, VersionedXmlDoc.fromBean(auditData).toXML());
						});
						Session.get().success(_T("Project forked"));
						setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(newProject));
					} else {
						target.add(form);
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
