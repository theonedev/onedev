package io.onedev.server.web.page.project.setting.code.analysis;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CodeAnalysisSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class CodeAnalysisSettingPage extends ProjectSettingPage {

	public CodeAnalysisSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		CodeAnalysisSetting bean = getProject().getCodeAnalysisSetting();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				getProject().setCodeAnalysisSetting(bean);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(CodeAnalysisSettingPage.class, CodeAnalysisSettingPage.paramsOf(getProject()));
				Session.get().success("Code analysis settings updated");
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Code Analysis Setting</span>").setEscapeModelStrings(false);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManage(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, CodeAnalysisSettingPage.class, paramsOf(project.getId()));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}
	
}
