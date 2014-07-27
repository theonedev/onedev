package com.pmease.gitplex.web.page.repository.info.code.tree;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;

public class RepoDescribePanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	static enum Mode {
		LABEL, EDITOR
	}
	
	private Mode mode = Mode.LABEL;
	
	public RepoDescribePanel(String id, IModel<Repository> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("serial")
	private Component newContent(String id) {
		if (mode == Mode.LABEL) {
			Fragment frag = new Fragment(id, "label", RepoDescribePanel.this);
			frag.add(new Label("description", new PropertyModel<String>(getDefaultModel(), "description")));
			frag.add(new AjaxLink<Void>("editlink") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					mode = Mode.EDITOR;
					onModeChanged(target);
				}
			});
			
			return frag;
		} else {
			Fragment frag = new Fragment(id, "editor", RepoDescribePanel.this);
			Form<?> form = new Form<Void>("form");
			form.add(new TextField<String>("input", new PropertyModel<String>(getRepository(), "description")));
			form.add(new AjaxButton("save", form) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					GitPlex.getInstance(RepositoryManager.class).save(getRepository());
					mode = Mode.LABEL;
					onModeChanged(target);
				}
			});
			
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					mode = Mode.LABEL;
					onModeChanged(target);
				}
				
			});
			
			frag.add(form);
			return frag;
		}
	}
	
	private Repository getRepository() {
		return (Repository) getDefaultModelObject();
	}
	
	private void onModeChanged(AjaxRequestTarget target) {
		Component c = newContent("description");
		addOrReplace(c);
		target.add(this);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newContent("description"));
	}
}
