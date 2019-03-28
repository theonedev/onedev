package io.onedev.server.web.page.project.setting.commitmessagetransform;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class CommitMessageTransformPage extends ProjectSettingPage {

	public CommitMessageTransformPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("commitMessageTransforms") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(ProjectManager.class).save(getProject());
				Session.get().success("Commit message transforms are updated");
			}
			
		};
		
		form.add(PropertyContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getProject();
			}

			@Override
			public void setObject(Serializable object) {
				Project editingProject = (Project) object; 
				getProject().setCommitMessageTransforms(editingProject.getCommitMessageTransforms());
			}
			
		}, "commitMessageTransforms"));

		add(form);
	}
	
}
