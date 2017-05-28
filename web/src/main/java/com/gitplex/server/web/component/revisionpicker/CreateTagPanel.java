package com.gitplex.server.web.component.revisionpicker;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;

@SuppressWarnings("serial")
abstract class CreateTagPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final String tagName;
	
	private final String revision;
	
	private String message;
	
	public CreateTagPanel(String id, IModel<Project> projectModel, String tagName, String revision) {
		super(id);
		this.projectModel = projectModel;
		this.tagName = tagName;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new TextArea<String>("message", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return message;
			}

			@Override
			public void setObject(String obj) {
				message = obj;
			}
			
		}));
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				User user = GitPlex.getInstance(UserManager.class).getCurrent();
				projectModel.getObject().tag(tagName, revision, user.asPerson(), message);
				onCreate(target, tagName);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}

	protected abstract void onCreate(AjaxRequestTarget target, String tagName);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

}
