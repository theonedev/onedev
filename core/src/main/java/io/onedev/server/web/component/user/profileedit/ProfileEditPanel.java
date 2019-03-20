package io.onedev.server.web.component.user.profileedit;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.util.ConfirmOnClick;

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
				editor.getBeanDescriptor().copyProperties(object, getUser());
			}
			
		}, Sets.newHashSet("password"), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User user = getUser();
				UserManager userManager = OneDev.getInstance(UserManager.class);
				User userWithSameName = userManager.findByName(user.getName());
				if (userWithSameName != null && !userWithSameName.equals(user)) {
					editor.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another user.");
				} 
				User userWithSameEmail = userManager.findByEmail(user.getEmail());
				if (userWithSameEmail != null && !userWithSameEmail.equals(user)) {
					editor.getErrorContext(new PathElement.Named("email"))
							.addError("This email has already been used by another user.");
				} 
				if (!editor.hasErrors(true)) {
					userManager.save(user, oldName);
					Session.get().success("Profile updated");
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				}
			}
			
		};	
		form.add(editor);
		
		form.add(new Link<Void>("delete") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getUser().equals(SecurityUtils.getUser()));
			}

			@Override
			public void onClick() {
				OneDev.getInstance(UserManager.class).delete(getUser());
				setResponsePage(UserListPage.class);
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete user '" + getUser().getDisplayName() + "'?")));

		add(form);
	}
}
