package io.onedev.server.web.component.user.passwordedit;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class PasswordEditPanel extends GenericPanel<User> {
	
	public PasswordEditPanel(String id, IModel<User> model) {
		super(id, model);
	}

	private User getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PasswordEditBean bean = new PasswordEditBean();
		
		Set<String> excludedProperties = new HashSet<>();
		
		// in case administrator changes password we do not ask for old password
		if (SecurityUtils.isAdministrator()) 
			excludedProperties.add("oldPassword");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				if (getUser().getPassword() != null)
					Session.get().success("Password has been changed");
				else
					Session.get().success("Password has been set");
					
				getUser().setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(bean.getNewPassword()));
				OneDev.getInstance(UserManager.class).update(getUser(), null);

				bean.setOldPassword(null);
				
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}

		};
		add(form);
		
		form.add(BeanContext.edit("editor", bean, excludedProperties, true));
		form.add(new Button("submit").add(AttributeAppender.append("value", new AbstractReadOnlyModel<>() {
			@Override
			public String getObject() {
				return getUser().getPassword()!=null?"Change":"Set";
			}
		})));
	}
	
}
