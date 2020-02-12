package io.onedev.server.web.component.build.authorization;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public abstract class ActionAuthorizationListPanel extends Panel {

	private final ActionAuthorizationsBean bean;
	
	public ActionAuthorizationListPanel(String id, ActionAuthorizationsBean bean) {
		super(id);
		this.bean = bean;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "actionAuthorizations");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Action authorization rules saved");
				onSaved(bean.getActionAuthorizations());
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	protected abstract void onSaved(List<ActionAuthorization> actionAuthorizations);
}
