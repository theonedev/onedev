package com.pmease.gitplex.web.page.repository.source.tree;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.source.component.RepositoryPanel;

public class RepositoryDescriptionPanel extends RepositoryPanel {
	private static final long serialVersionUID = 1L;
	
	static enum Mode {
		LABEL, EDITOR
	}
	
	private Mode mode = Mode.LABEL;
	
	public RepositoryDescriptionPanel(String id, IModel<Repository> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("serial")
	private Component newContent(String id) {
		if (mode == Mode.LABEL) {
			Fragment frag = new Fragment(id, "label", RepositoryDescriptionPanel.this);
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
			Fragment frag = new Fragment(id, "editor", RepositoryDescriptionPanel.this);
			Form<?> form = new Form<Void>("form");
			form.add(new TextField<String>("input", new PropertyModel<String>(RepositoryDescriptionPanel.this.getDefaultModel(), "description")));
			form.add(new AjaxButton("save", form) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					Repository repository = getRepository();
					GitPlex.getInstance(RepositoryManager.class).save(repository);
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
