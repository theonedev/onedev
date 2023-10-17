package io.onedev.server.web.component.user.profileedit;

import java.io.Serializable;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.user.UserDeleteLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
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
			
		}, Sets.newHashSet("password", "guest"), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User user = getUser();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				User userWithSameName = userManager.findByName(user.getName());
				if (userWithSameName != null && !userWithSameName.equals(user)) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 
				
				if (editor.isValid()) {
					userManager.update(user, oldName);
					Session.get().success("Profile updated");
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				}
			}
			
		};	
		form.add(editor);
		
		form.add(new FencedFeedbackPanel("feedback", form).setEscapeModelStrings(false));
		
		boolean canDelete;
		if (getPage() instanceof MyProfilePage) {
			canDelete = !getUser().isRoot()
					&& SecurityUtils.getPrevUserId().equals(0L)
					&& OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfDeregister();
		} else {
			canDelete = !getUser().isRoot() && !getUser().equals(SecurityUtils.getUser());
		}
		form.add(new UserDeleteLink("delete") {

			@Override
			protected User getUser() {
				return ProfileEditPanel.this.getUser();
			}

		}.setVisible(canDelete));

		add(form);
	}
}
