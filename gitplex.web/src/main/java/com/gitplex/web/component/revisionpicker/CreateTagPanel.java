package com.gitplex.web.component.revisionpicker;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.manager.AccountManager;

@SuppressWarnings("serial")
abstract class CreateTagPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final String tagName;
	
	private final String revision;
	
	private String message;
	
	public CreateTagPanel(String id, IModel<Depot> depotModel, String tagName, String revision) {
		super(id);
		this.depotModel = depotModel;
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

				Account user = GitPlex.getInstance(AccountManager.class).getCurrent();
				depotModel.getObject().tag(tagName, revision, user.asPerson(), message);
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
		depotModel.detach();
		super.onDetach();
	}

}
