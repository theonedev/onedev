package com.pmease.gitop.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class ProjectPage extends AbstractLayoutPage {

	public ProjectPage() {
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		final Project project = Gitop.getInstance(ProjectManager.class).load(1L);
		
		final EditContext editContext = EditableUtils.getContext(project);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				editContext.validate();
				if (!editContext.hasValidationError()) {
				    Project reloaded = Gitop.getInstance(ProjectManager.class).load(1L);
				    EditableUtils.copyProperties(project, reloaded);
				    Gitop.getInstance(ProjectManager.class).save(reloaded);
				}
			}
			
		};
		
		form.add((Component) editContext.renderForEdit("editor"));
		
		add(form);
	}
	
	@Override
	protected String getPageTitle() {
		return "Test page used by Robin";
	}

}
