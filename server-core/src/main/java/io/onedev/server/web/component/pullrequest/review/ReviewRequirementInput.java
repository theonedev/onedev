package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;

@SuppressWarnings("serial")
public class ReviewRequirementInput extends TextField<String> {

	private final IModel<Project> projectModel;
	
	public ReviewRequirementInput(String id, IModel<Project> projectModel, IModel<String> specModel) {
		super(id, specModel);
		
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ReviewRequirementBehavior(projectModel));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

}
