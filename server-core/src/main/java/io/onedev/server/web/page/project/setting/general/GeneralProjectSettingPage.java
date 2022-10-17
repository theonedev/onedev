package io.onedev.server.web.page.project.setting.general;

import static io.onedev.server.model.Project.PROP_CODE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_NAME;

import java.io.Serializable;
import java.util.Collection;

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
import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.project.ConfirmDeleteProjectModal;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.editablebean.LabelsBean;

@SuppressWarnings("serial")
public class GeneralProjectSettingPage extends ProjectSettingPage {

	private BeanEditor editor;
	
	public GeneralProjectSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_DESCRIPTION, 
				PROP_CODE_MANAGEMENT, PROP_ISSUE_MANAGEMENT);
		
		DefaultRoleBean defaultRoleBean = new DefaultRoleBean();
		defaultRoleBean.setRole(getProject().getDefaultRole());
		
		LabelsBean labelsBean = LabelsBean.of(getProject());
		
		ParentBean parentBean = new ParentBean();
		if (getProject().getParent() != null)
			parentBean.setParentPath(getProject().getParent().getPath());
		
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

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
		
		BeanEditor defaultRoleEditor = BeanContext.edit("defaultRoleEditor", defaultRoleBean);		
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
						Project parent = getProjectManager().findByPath(parentBean.getParentPath());
						if (parent == null) 
							parentError = "Parent project not found";
						else if (project.isSelfOrAncestorOf(parent)) 
							parentError = "Can not use current or descendant project as parent";
						else if (!SecurityUtils.canCreateChildren(parent)) 
							parentError = "Not authorized to move project under this parent";
						else
							project.setParent(parent);
					} else if (!SecurityUtils.canCreateRootProjects()) {
						parentError = "Not authorized to set as root project";
					} else {
						project.setParent(null);
					}
				}
				
				if (parentError != null) {
					parentEditor.error(new Path(new PathNode.Named("parentPath")), parentError);
				} else {
					Project projectWithSameName = getProjectManager().find(project.getParent(), project.getName());
					if (projectWithSameName != null && !projectWithSameName.equals(project)) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another project");
					} else {
						project.setDefaultRole(defaultRoleBean.getRole());
						OneDev.getInstance(TransactionManager.class).run(new Runnable() {

							@Override
							public void run() {
								getProjectManager().save(project);
								OneDev.getInstance(ProjectLabelManager.class).sync(getProject(), labelsBean.getLabels());
							}
							
						});
						Session.get().success("General setting has been updated");
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
				new ConfirmDeleteProjectModal(target) {
					
					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Project.class);
						if (redirectUrlAfterDelete != null)
							throw new RedirectToUrlException(redirectUrlAfterDelete);
						else
							setResponsePage(ProjectListPage.class);
					}
					
					@Override
					protected Project getProject() {
						return GeneralProjectSettingPage.this.getProject();
					}

				};
			}
			
		});
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "General Setting").add(AttributeAppender.replace("class", "text-truncate"));
	}

}
