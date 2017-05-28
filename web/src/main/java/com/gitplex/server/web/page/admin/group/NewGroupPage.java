package com.gitplex.server.web.page.admin.group;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.admin.AdministrationPage;
import com.gitplex.server.web.page.group.GroupMembershipsPage;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class NewGroupPage extends AdministrationPage {

	private Group group = new Group();
	
	private CheckBox administratorInput;
	
	private CheckBox canCreateProjectsInput;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", group, 
				Sets.newHashSet("administrator", "canCreateProjects"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
				Group groupWithSameName = groupManager.find(group.getName());
				if (groupWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another group");
				} 
				if (!editor.hasErrors(true)) {
					group.setAdministrator(administratorInput.getModelObject());
					group.setCanCreateProjects(canCreateProjectsInput.getModelObject());
					groupManager.save(group, null);
					Session.get().success("Group created");
					setResponsePage(GroupMembershipsPage.class, GroupMembershipsPage.paramsOf(group));
				}
			}
			
		};
		form.add(editor);
		administratorInput = new CheckBox("administrator", Model.of(group.isAdministrator()));
		administratorInput.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (administratorInput.getModelObject())
					canCreateProjectsInput.setModelObject(true);
				target.add(canCreateProjectsInput);
			}
			
		});
		form.add(administratorInput);
		
		canCreateProjectsInput = new CheckBox("canCreateProjects", Model.of(group.isCanCreateProjects())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!administratorInput.getModelObject());
			}
			
		};
		canCreateProjectsInput.setOutputMarkupId(true);
		form.add(canCreateProjectsInput);			
		add(form);
	}

}
