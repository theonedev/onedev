package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ActionAuthorizationsPage extends BuildSettingPage {

	public ActionAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ActionAuthorizationsBean bean = new ActionAuthorizationsBean();
		bean.setActionAuthorizations(getProject().getBuildSetting().getActionAuthorizations());
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "actionAuthorizations");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Action authorization rules saved");
				getProject().getBuildSetting().setActionAuthorizations(bean.getActionAuthorizations());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ActionAuthorizationsPage.class, 
						ActionAuthorizationsPage.paramsOf(getProject()));
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

}
