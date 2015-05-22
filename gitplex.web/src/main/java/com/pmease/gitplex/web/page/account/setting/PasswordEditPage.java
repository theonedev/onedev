package com.pmease.gitplex.web.page.account.setting;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
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
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				GitPlex.getInstance(UserManager.class).save(getAccount());
				Session.get().success("Password has been changed");
				backToPrevPage();
			}
			
		};
		add(form);
		
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
