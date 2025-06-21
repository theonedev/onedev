package io.onedev.server.web.page.project;

import static io.onedev.server.model.Project.PROP_CODE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_KEY;
import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_PACK_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_TIME_TRACKING;
import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.BaseAuthorizationManager;
import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import io.onedev.server.web.page.project.setting.general.DefaultRolesBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;
import io.onedev.server.web.util.editbean.LabelsBean;

public class NewProjectPage extends LayoutPage {

	private static final String PARAM_PARENT = "parent";
	
	private final Long parentId;
	
	public NewProjectPage(PageParameters params) {
		super(params);
		parentId = params.get(PARAM_PARENT).toOptionalLong();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project editProject = new Project();
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_KEY, PROP_DESCRIPTION, 
				PROP_CODE_MANAGEMENT, PROP_PACK_MANAGEMENT, PROP_ISSUE_MANAGEMENT, 
				PROP_TIME_TRACKING);
		
		DefaultRolesBean defaultRolesBean = new DefaultRolesBean();
		LabelsBean labelsBean = new LabelsBean();
		ParentBean parentBean = new ParentBean();
		if (parentId != null)
			parentBean.setParentPath(getProjectManager().load(parentId).getPath());
		
		BeanEditor editor = BeanContext.edit("editor", editProject, properties, false);
		BeanEditor labelsEditor = BeanContext.edit("labelsEditor", labelsBean);
		BeanEditor parentEditor = BeanContext.edit("parentEditor", parentBean);
		if (parentId != null)
			parentEditor.setVisible(false);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				try {
					String projectPath = editProject.getName();
					if (parentBean.getParentPath() != null)
						projectPath = parentBean.getParentPath() + "/" + projectPath;
					Project newProject = getProjectManager().setup(projectPath);
					if (editProject.getKey() != null && getProjectManager().findByKey(editProject.getKey()) != null) {
						editor.error(new Path(new PathNode.Named(PROP_KEY)),
								_T("This key has already been used by another project"));
					}
					if (!newProject.isNew()) {
						editor.error(new Path(new PathNode.Named(PROP_NAME)),
								_T("This name has already been used by another project"));
					}
					if (editor.isValid()) {
						newProject.setKey(editProject.getKey());
						newProject.setDescription(editProject.getDescription());
						newProject.setCodeManagement(editProject.isCodeManagement());
						newProject.setIssueManagement(editProject.isIssueManagement());
						newProject.setPackManagement(editProject.isPackManagement());
						newProject.setTimeTracking(editProject.isTimeTracking());
						
						OneDev.getInstance(TransactionManager.class).run(() -> {
							getProjectManager().create(newProject);
							OneDev.getInstance(BaseAuthorizationManager.class).syncRoles(newProject, defaultRolesBean.getRoles());
							OneDev.getInstance(ProjectLabelManager.class).sync(newProject, labelsBean.getLabels());

							var auditData = editor.getPropertyValues();
							auditData.put("parent", parentBean.getParentPath());
							auditData.put("labels", labelsBean.getLabels());
							auditData.put("defaultRoles", defaultRolesBean.getRoleNames());
							OneDev.getInstance(AuditManager.class).audit(newProject, "created project", null, VersionedXmlDoc.fromBean(newProject).toXML());
						});
						
						Session.get().success(_T("New project created"));
						if (newProject.isCodeManagement())
							setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(newProject));
						else if (newProject.isIssueManagement())
							setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(newProject));
						else if (newProject.isPackManagement())
							setResponsePage(ProjectPacksPage.class, ProjectPacksPage.paramsOf(newProject));
						else
							setResponsePage(ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(newProject));
					}
				} catch (UnauthorizedException e) {
					if (parentEditor.isVisible())
						parentEditor.error(new Path(new PathNode.Named("parentPath")), e.getMessage());
					else
						throw e;
				}
			}
			
		};
		form.add(editor);
		form.add(labelsEditor);
		form.add(BeanContext.edit("defaultRoleEditor", defaultRolesBean));
		form.add(parentEditor);
		add(form);
	}

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getAuthUser() != null;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		if (parentId != null)
			return new Label(componentId, _T("Create Child Project"));
		else
			return new Label(componentId, _T("Create Project"));
	}
	
	public static PageParameters paramsOf(Project parent) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PARENT, parent.getId());
		return params;
	}
	
}
