package io.onedev.server.web.page.admin.role;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class NewRolePage extends AdministrationPage {

	private Role role = new Role();
	
	public NewRolePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.edit("editor", role);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				RoleManager roleManager = OneDev.getInstance(RoleManager.class);
				Role roleWithSameName = roleManager.find(role.getName());
				if (roleWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another role");
				} 
				if (editor.isValid()) {
					roleManager.save(role, null);
					Session.get().success("Role created");
					setResponsePage(RoleListPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("roles", RoleListPage.class));
		return fragment;
	}
	
}
