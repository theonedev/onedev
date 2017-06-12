package com.gitplex.server.web.page.group;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanDescriptor;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.project.ProjectListPage;
import com.gitplex.server.web.util.ConfirmOnClick;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class GroupProfilePage extends GroupPage {

	private BeanEditor<?> editor;
	
	private CheckBox administratorInput;
	
	private CheckBox canCreateProjectsInput;
	
	private String oldName;
	
	public GroupProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (SecurityUtils.isAdministrator()) {
			Fragment fragment = new Fragment("content", "editFrag", this);
			editor = BeanContext.editModel("editor", new IModel<Serializable>() {

				@Override
				public void detach() {
				}

				@Override
				public Serializable getObject() {
					return getGroup();
				}

				@Override
				public void setObject(Serializable object) {
					// check contract of GroupManager.save on why we assign oldName here
					oldName = getGroup().getName();
					editor.getBeanDescriptor().copyProperties(object, getGroup());
				}
				
			}, Sets.newHashSet("administrator", "canCreateProjects"));

			Form<?> form = new Form<Void>("form") {

				@Override
				protected void onSubmit() {
					super.onSubmit();
					
					Group group = getGroup();
					group.setAdministrator(administratorInput.getModelObject());
					if (group.isAdministrator())
						group.setCanCreateProjects(true);
					else
						group.setCanCreateProjects(canCreateProjectsInput.getModelObject());
					GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
					Group groupWithSameName = groupManager.find(group.getName());
					if (groupWithSameName != null && !groupWithSameName.equals(group)) {
						editor.getErrorContext(new PathSegment.Property("name"))
								.addError("This name has already been used by another group.");
					} 
					if (!editor.hasErrors(true)) {
						groupManager.save(group, oldName);
						setResponsePage(GroupProfilePage.class, GroupProfilePage.paramsOf(group));
					}
				}
				
			};	
			form.add(editor);
			
			administratorInput = new CheckBox("administrator", Model.of(getGroup().isAdministrator()));
			administratorInput.add(new OnChangeAjaxBehavior() {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					if (administratorInput.getModelObject())
						canCreateProjectsInput.setModelObject(true);
					target.add(canCreateProjectsInput);
				}
				
			});
			form.add(administratorInput);
			
			canCreateProjectsInput = new CheckBox("canCreateProjects", Model.of(getGroup().isCanCreateProjects())) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(!administratorInput.getModelObject());
				}
				
			};
			canCreateProjectsInput.setOutputMarkupId(true);
			form.add(canCreateProjectsInput);			
			
			form.add(new Link<Void>("delete") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					
					setVisible(SecurityUtils.isAdministrator());
				}

				@Override
				public void onClick() {
					GitPlex.getInstance(GroupManager.class).delete(getGroup());
					setResponsePage(ProjectListPage.class);
				}
				
			}.add(new ConfirmOnClick("Do you really want to delete group '" + getGroup().getName() + "'?")));
			
			fragment.add(form);
			add(fragment);
		} else {
			Group group = new Group();
			new BeanDescriptor(Group.class).copyProperties(getGroup(), group);
			add(BeanContext.viewBean("content", group));
		}

	}

}
