package io.onedev.server.web.page.project.setting.general;

import static io.onedev.server.model.Project.PROP_CODE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_KEY;
import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_PACK_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_TIME_TRACKING;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.editbean.LabelsBean;

public class GeneralProjectSettingPage extends ProjectSettingPage {

	private BeanEditor editor;
	
	public GeneralProjectSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_KEY, 
				PROP_DESCRIPTION, PROP_CODE_MANAGEMENT, PROP_PACK_MANAGEMENT, 
				PROP_ISSUE_MANAGEMENT, PROP_TIME_TRACKING);
		
		DefaultRolesBean defaultRolesBean = new DefaultRolesBean();
		defaultRolesBean.setRoles(getProject().getBaseAuthorizations().stream().map(it->it.getRole()).collect(Collectors.toList()));
		
		LabelsBean labelsBean = LabelsBean.of(getProject());
		
		ParentBean parentBean = new ParentBean();
		if (getProject().getParent() != null)
			parentBean.setParentPath(getProject().getParent().getPath());
		
		editor = BeanContext.editModel("editor", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getProject();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getDescriptor().copyProperties(object, getProject());
			}

		}, properties, false);
		
		var auditData = editor.getPropertyValues();
		auditData.put("defaultRoles", defaultRolesBean.getRoleNames());
		auditData.put("labels", labelsBean.getLabels());
		auditData.put("parent", parentBean.getParentPath());

		var oldAuditContent = VersionedXmlDoc.fromBean(auditData).toXML();		

		BeanEditor defaultRoleEditor = BeanContext.edit("defaultRoleEditor", defaultRolesBean);		
		BeanEditor labelsEditor = BeanContext.edit("labelsEditor", labelsBean);
		BeanEditor parentEditor = BeanContext.edit("parentEditor", parentBean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Project project = getProject();
				String prevParentPath;
				if (project.getParent() != null)
					prevParentPath = project.getParent().getPath();
				else
					prevParentPath = null;

				String parentError = null;
				if (!Objects.equal(prevParentPath, parentBean.getParentPath())) {
					if (parentBean.getParentPath() != null) {
						Project parent = getProjectService().findByPath(parentBean.getParentPath());
						if (parent == null) 
							parentError = _T("Parent project not found");
						else if (project.isSelfOrAncestorOf(parent)) 
							parentError = _T("Can not use current or descendant project as parent");
						else if (!SecurityUtils.canCreateChildren(parent)) 
							parentError = _T("Not authorized to move project under this parent");
						else
							project.setParent(parent);
					} else if (!SecurityUtils.canCreateRootProjects()) {
						parentError = _T("Not authorized to set as root project");
					} else {
						project.setParent(null);
					}
				}
				
				if (parentError != null) {
					parentEditor.error(new Path(new PathNode.Named("parentPath")), parentError);
				} else {
					var projectWithSameName = getProjectService().find(project.getParent(), project.getName());
					if (projectWithSameName != null && !projectWithSameName.equals(project)) {
						editor.error(new Path(new PathNode.Named(PROP_NAME)),
								_T("This name has already been used by another project"));
					} 
					if (project.getKey() != null) {
						var projectWithSameKey = getProjectService().findByKey(project.getKey());
						if (projectWithSameKey != null && !projectWithSameKey.equals(project)) {
							editor.error(new Path(new PathNode.Named(PROP_KEY)),
									_T("This key has already been used by another project"));
						}
					}
					if (editor.isValid()) {
						OneDev.getInstance(TransactionService.class).run(new Runnable() {

							@Override
							public void run() {
								var project = getProject();
								getProjectService().update(project);
								OneDev.getInstance(BaseAuthorizationService.class).syncRoles(project, defaultRolesBean.getRoles());
								OneDev.getInstance(ProjectLabelService.class).sync(project, labelsBean.getLabels());
								var auditData = editor.getPropertyValues();
								auditData.put("defaultRoles", defaultRolesBean.getRoleNames());
								auditData.put("labels", labelsBean.getLabels());
								auditData.put("parent", parentBean.getParentPath());
								var newAuditContent = VersionedXmlDoc.fromBean(auditData).toXML();
								auditService.audit(project, "changed general settings", oldAuditContent, newAuditContent);
							}
							
						});
						Session.get().success(_T("General settings updated"));
						setResponsePage(GeneralProjectSettingPage.class, paramsOf(project));
					}
				}
				
			}
			
		};
		form.add(editor);
		form.add(defaultRoleEditor);
		form.add(labelsEditor);
		form.add(parentEditor);
		
		form.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ConfirmModalPanel(target) {

					@Override
					protected void onConfirm(AjaxRequestTarget target) {
						Project project = getProject();
						OneDev.getInstance(ProjectService.class).delete(project);
						var oldAuditContent = VersionedXmlDoc.fromBean(project).toXML();
						if (project.getParent() != null)
							auditService.audit(project.getParent(), "deleted child project \"" + project.getName() + "\" via RESTful API", oldAuditContent, null);
						else
							auditService.audit(null, "deleted root project \"" + project.getName() + "\" via RESTful API", oldAuditContent, null);
						
						getSession().success(MessageFormat.format(_T("Project \"{0}\" deleted"), project.getPath()));

						String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Project.class);
						if (redirectUrlAfterDelete != null)
							throw new RedirectToUrlException(redirectUrlAfterDelete);
						else
							setResponsePage(ProjectListPage.class);
					}

					@Override
					protected String getConfirmMessage() {
						return MessageFormat.format(_T("Everything inside this project and all child projects will be deleted and can not be recovered, "
								+ "please type project path <code>{0}</code> below to confirm deletion."), getProject().getPath());
					}

					@Override
					protected String getConfirmInput() {
						return getProject().getPath();
					}
					
				};
			}
			
		});
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("General Settings")).add(AttributeAppender.replace("class", "text-truncate"));
	}

}
