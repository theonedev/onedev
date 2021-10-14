package io.onedev.server.web.page.project;

import java.util.Collection;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import static io.onedev.server.model.Project.*;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.setting.general.DefaultRoleBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;

@SuppressWarnings("serial")
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
		
		Project project = new Project();
		
		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_DESCRIPTION, 
				PROP_CODE_MANAGEMENT_ENABLED, PROP_ISSUE_MANAGEMENT_ENABLED);
		
		DefaultRoleBean defaultRoleBean = new DefaultRoleBean();
		ParentBean parentBean = new ParentBean();
		
		BeanEditor editor = BeanContext.edit("editor", project, properties, false);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project parent;
				if (parentId != null) {
					parent = projectManager.load(parentId); 
				} else {
					parent = parentBean.getParent();
					if (parent != null && !SecurityUtils.canCreateChildren(parent) 
							|| parent == null && !SecurityUtils.canCreateRootProjects()) {
						throw new UnauthorizedException();
					}
				}
				project.setParent(parent);
				Project projectWithSameName = projectManager.find(parent, project.getName());
				if (projectWithSameName != null) {
					if (parent != null) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another child project");
					} else {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another root project");
					}
				} else {
					project.setDefaultRole(defaultRoleBean.getRole());
					projectManager.create(project);
					Session.get().success("New project created");
					if (project.isCodeManagementEnabled())
						setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
					else if (project.isIssueManagementEnabled())
						setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project));
					else
						setResponsePage(ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(project));
				}
			}
			
		};
		form.add(editor);
		
		form.add(BeanContext.edit("defaultRoleEditor", defaultRoleBean));
		
		if (parentId != null)
			form.add(new WebMarkupContainer("parentEditor").setVisible(false));
		else
			form.add(BeanContext.edit("parentEditor", parentBean));			
		
		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getUser() != null;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Create Project");
	}
	
	public static PageParameters paramsOf(Project parent) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PARENT, parent.getId());
		return params;
	}
	
}
