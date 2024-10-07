package io.onedev.server.web.component.user.preferences;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

import static io.onedev.server.model.User.PROP_DISABLE_WATCH_NOTIFICATIONS;
import static io.onedev.server.model.User.PROP_NOTIFY_OWN_EVENTS;

@SuppressWarnings("serial")
public class PreferencesEditPanel extends GenericPanel<User> {

	private BeanEditor editor;
	
	public PreferencesEditPanel(String id, IModel<User> model) {
		super(id, model);
	}

	private User getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		editor = BeanContext.editModel("editor", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getUser();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getDescriptor().copyProperties(object, getUser());
			}

		}, Sets.newHashSet(PROP_DISABLE_WATCH_NOTIFICATIONS, PROP_NOTIFY_OWN_EVENTS), false);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User user = getUser();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				userManager.update(user, null);
				Session.get().success("Preferences updated");
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}
			
		};	
		form.add(editor);
		form.add(new FencedFeedbackPanel("feedback", form).setEscapeModelStrings(false));
		add(form);
	}
}
