package io.onedev.server.web.page.project.setting.webhook;

import java.io.Serializable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class WebHooksPage extends ProjectSettingPage {

	public WebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHooksEditBean bean = new WebHooksEditBean();
		bean.setWebHooks(getProject().getWebHooks());
		PropertyEditor<Serializable> editor = 
				PropertyContext.editBean("editor", bean, "webHooks");
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getProject().setWebHooks(bean.getWebHooks());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				getSession().success("Web hooks saved");
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WebHookCssResourceReference()));
	}

}
