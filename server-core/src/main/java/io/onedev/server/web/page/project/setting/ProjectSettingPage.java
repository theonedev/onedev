package io.onedev.server.web.page.project.setting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.admin.AdministrationCssResourceReference;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.overview.ProjectOverviewPage;

public abstract class ProjectSettingPage extends ProjectPage {

	public ProjectSettingPage(PageParameters params) {
		super(params);

		if (isProjectDefaults() && !OneDev.getInstance(SubscriptionService.class).isSubscriptionActive())
			throw new NoSubscriptionException("Project defaults");
	}

	@Override
	protected boolean isPermitted() {
		if (isProjectDefaults())
			return SecurityUtils.isAdministrator();
		else
			return SecurityUtils.canManageProject(getProject());
	}
	
	protected boolean isProjectDefaults() {
		return Project.DEFAULT_ID.equals(getProject().getId());
	}

	protected boolean hasProjectDefaults() {
		return true;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		if (isProjectDefaults()) {
			var fragment = new Fragment(componentId, "defaultsTitleFrag", this);
			fragment.add(new Label("defaults", _T("Project Defaults")));
			fragment.add(newProjectTitle("setting"));
			return fragment;
		}
		var fragment = new Fragment(componentId, "projectTitleWithDefaultsHintFrag", this);
		fragment.add(super.newTopbarTitle("title").setRenderBodyOnly(true));
		fragment.add(new WebMarkupContainer("defaultsHint")
				.setVisible(hasProjectDefaults() && getProject().getParent() == null && !OneDev.getInstance(SubscriptionService.class).isSubscriptionActive()));
		return fragment;
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project))
			return new ViewStateAwarePageLink<Void>(componentId, getPageClass(), paramsOf(project.getId()));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectOverviewPage.class, ProjectPage.paramsOf(project.getId()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new ProjectSettingResourceReference()));
		if (isProjectDefaults())
			response.render(CssHeaderItem.forReference(new AdministrationCssResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return isProjectDefaults() ? "Project Defaults - Administration" : "Settings - " + getProject().getPath();
	}
	
}
