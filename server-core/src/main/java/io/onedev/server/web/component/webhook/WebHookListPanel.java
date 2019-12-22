package io.onedev.server.web.component.webhook;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public abstract class WebHookListPanel extends Panel {

	private final WebHooksBean bean;
	
	public WebHookListPanel(String id, WebHooksBean bean) {
		super(id);
		this.bean = bean;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "webHooks");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Web hooks saved");
				onSaved(bean.getWebHooks());
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WebHookListCssResourceReference()));
	}

	protected abstract void onSaved(ArrayList<WebHook> webHooks);
}
