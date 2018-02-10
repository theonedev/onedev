package com.turbodev.server.web.page.project.setting.commitmessagetransform;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.support.CommitMessageTransformSetting;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.reflection.ReflectionPropertyEditor;
import com.turbodev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class CommitMessageTransformPage extends ProjectSettingPage {

	public CommitMessageTransformPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("commitMessageTransformSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				TurboDev.getInstance(ProjectManager.class).save(getProject());
				Session.get().success("Commit message transform setting is updated");
			}
			
		};
		form.add(new ReflectionPropertyEditor("editor", new PropertyDescriptor(Project.class, "commitMessageTransformSetting"), new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getProject().getCommitMessageTransformSetting();
			}

			@Override
			public void setObject(Serializable object) {
				getProject().setCommitMessageTransformSetting((CommitMessageTransformSetting) object);
			}

		}));

		add(form);
	}
	
}
