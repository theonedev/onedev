package io.onedev.server.web.page.project.setting.build;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

public class DefaultFixedIssueFiltersPage extends ProjectBuildSettingPage {

	public DefaultFixedIssueFiltersPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DefaultFixedIssueFiltersBean bean = new DefaultFixedIssueFiltersBean();
		bean.setDefaultFixedIssueFilters(getProject().getBuildSetting().getDefaultFixedIssueFilters());
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "defaultFixedIssueFilters");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success(_T("Default fixed issue filters saved"));
				getProject().getBuildSetting().setDefaultFixedIssueFilters(bean.getDefaultFixedIssueFilters());
				OneDev.getInstance(ProjectManager.class).update(getProject());
				setResponsePage(DefaultFixedIssueFiltersPage.class, 
						DefaultFixedIssueFiltersPage.paramsOf(getProject()));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Default Fixed Issue Filters"));
	}

}
