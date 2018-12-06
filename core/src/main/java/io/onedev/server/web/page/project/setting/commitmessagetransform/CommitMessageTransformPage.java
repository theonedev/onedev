package io.onedev.server.web.page.project.setting.commitmessagetransform;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class CommitMessageTransformPage extends ProjectSettingPage {

	public CommitMessageTransformPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		CommitMessageTransformSettingHolder bean = new CommitMessageTransformSettingHolder();
		bean.setCommitMessageTransformSetting(getProject().getCommitMessageTransformSetting());
		
		Form<?> form = new Form<Void>("commitMessageTransformSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getProject().setCommitMessageTransformSetting(bean.getCommitMessageTransformSetting());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				Session.get().success("Commit message transform setting is updated");
			}
			
		};
		
		form.add(BeanContext.editBean("editor", bean));

		add(form);
	}
	
}
