package com.gitplex.server.web.page.project.setting.commitmessagetransform;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.support.CommitMessageTransformSetting;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.reflection.ReflectionPropertyEditor;
import com.gitplex.server.web.page.project.setting.ProjectSettingPage;

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
				GitPlex.getInstance(ProjectManager.class).save(getProject());
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
