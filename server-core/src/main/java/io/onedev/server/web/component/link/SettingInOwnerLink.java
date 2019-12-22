package io.onedev.server.web.component.link;

import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.project.ProjectListPage;

public class SettingInOwnerLink extends BookmarkablePageLink<Project> {

	private static final long serialVersionUID = 1L;
	
	public SettingInOwnerLink(String id, IModel<Project> projectModel, Class<? extends Page> userSettingPageClass, 
			Class<? extends Page> mySettingPageClass) {
		super(id, getPageClass(projectModel.getObject(), userSettingPageClass, mySettingPageClass), 
				getPageParameters(projectModel.getObject(), userSettingPageClass, mySettingPageClass));
		setModel(projectModel);
	}

	private static Class<? extends Page> getPageClass(Project project, Class<? extends Page> userSettingPageClass, 
			Class<? extends Page> mySettingPageClass) {
		if (SecurityUtils.isAdministrator())
			return userSettingPageClass;
		else if (project.getOwner().equals(SecurityUtils.getUser()))
			return mySettingPageClass;
		else
			return ProjectListPage.class;
	}
	
	private static PageParameters getPageParameters(Project project, Class<? extends Page> userSettingPageClass, 
			Class<? extends Page> mySettingPageClass) {
		if (SecurityUtils.isAdministrator())
			return UserPage.paramsOf(project.getOwner());
		else 
			return new PageParameters();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.isAdministrator() || getModelObject().getOwner().equals(SecurityUtils.getUser()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		onConfigure();
		if (!isEnabled())
			tag.setName("span");
	}
	
}
