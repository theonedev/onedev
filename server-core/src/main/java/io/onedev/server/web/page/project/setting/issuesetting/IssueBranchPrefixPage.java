package io.onedev.server.web.page.project.setting.issuesetting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.overview.ProjectOverviewPage;

public class IssueBranchPrefixPage extends ProjectIssueSettingPage {

	public IssueBranchPrefixPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		ProjectIssueSetting bean = getProject().getIssueSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().setIssueSetting(bean);
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed issue branch prefix", oldAuditContent, newAuditContent);
				setResponsePage(IssueBranchPrefixPage.class, IssueBranchPrefixPage.paramsOf(getProject()));
				Session.get().success(_T("Issue branch prefix updated"));
			}

		};
		form.add(BeanContext.edit("editor", bean));

		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Issue Branch Prefix"));
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project))
			return new ViewStateAwarePageLink<Void>(componentId, IssueBranchPrefixPage.class, paramsOf(project.getId()));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectOverviewPage.class, ProjectPage.paramsOf(project.getId()));
	}

}
