package io.onedev.server.web.component.user.profileedit;

import static io.onedev.server.model.User.PROP_NOTIFY_OWN_EVENTS;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

public class ProfileEditPanel extends GenericPanel<User> {

	private BeanEditor editor;
	
	private String oldName;
	
	public ProfileEditPanel(String id, IModel<User> model) {
		super(id, model);
	}

	private User getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		var excludeProperties = Sets.newHashSet("password", "serviceAccount");
		if (getUser().isServiceAccount() || getUser().isDisabled())
			excludeProperties.add(PROP_NOTIFY_OWN_EVENTS);
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getUser();
			}

			@Override
			public void setObject(Serializable object) {
				// check contract of UserManager.save on why we assign oldName here
				oldName = getUser().getName();
				editor.getDescriptor().copyProperties(object, getUser());
			}
			
		}, excludeProperties, true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User user = getUser();
				
				User userWithSameName = getUserManager().findByName(user.getName());
				if (userWithSameName != null && !userWithSameName.equals(user)) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							_T("Login name already used by another account"));
				} 
				
				if (editor.isValid()) {
					getUserManager().update(user, oldName);
					Session.get().success(_T("Profile updated"));
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				}
			}
			
		};	
		form.add(editor);
		
		form.add(new FencedFeedbackPanel("feedback", form));		
		form.add(new WebMarkupContainer("submit").add(AttributeAppender.append("value", _T("Save"))));
		
		add(form);
	}

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}

}
