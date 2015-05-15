package com.pmease.gitplex.web.page.account.setting;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.editable.DefaultBeanDescriptor;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.reflection.ReflectionBeanEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class PasswordEditPage extends AccountSettingPage {
	
	public PasswordEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User account = getAccount();
		
		sidebar.add(new Label("title", "Change Password of " + account.getDisplayName()));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				GitPlex.getInstance(UserManager.class).save(getAccount());
				Session.get().success("Password has been changed");
				backToPrevPage();
			}
			
		};
		sidebar.add(form);
		
		final PasswordDescriptor descriptor = new PasswordDescriptor();
		form.add(new ReflectionBeanEditor("editor", descriptor, new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getAccount();
			}

			@Override
			public void setObject(Serializable object) {
				descriptor.copyProperties(object, getAccount());
			}
			
		}));
		
		form.add(new Link<Void>("cancel") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(prevPageRef != null);
			}

			@Override
			public void onClick() {
				backToPrevPage();
			}
			
		});
	}

	private static class PasswordDescriptor extends DefaultBeanDescriptor {

		public PasswordDescriptor() {
			super(User.class);
			
			for (Iterator<PropertyDescriptor> it = propertyDescriptors.iterator(); it.hasNext();) {
				if (!it.next().getPropertyName().equals("password"))
					it.remove();
			}
		}
		
	}
	
}
