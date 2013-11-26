package com.pmease.gitop.web.page.project.source.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;

public class ProjectDescriptionPanel extends ProjectPanel {
	private static final long serialVersionUID = 1L;
	
	public ProjectDescriptionPanel(String id, IModel<Project> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("description", new PropertyModel<String>(getDefaultModel(), "description")));
		
		WebMarkupContainer editLink = new WebMarkupContainer("editlink");
		add(editLink);
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", false) {

			@Override
			protected Component newContent(String id) {
				final DropdownPanel theDropdown = this;
				Fragment frag = new Fragment(id, "descriptionEditor", ProjectDescriptionPanel.this);
				Form<?> form = new Form<Void>("form");
				form.add(new TextField<String>("input", new PropertyModel<String>(ProjectDescriptionPanel.this.getDefaultModel(), "description")));
				form.add(new AjaxButton("save", form) {
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						Project project = getProject();
						Gitop.getInstance(ProjectManager.class).save(project);
						theDropdown.close(target);
						target.add(ProjectDescriptionPanel.this);
					}
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						theDropdown.close(target);
					}
					
				});
				
				frag.add(form);
				return frag;
			}
		};
		
		add(dropdown);
		
		editLink.add(new DropdownBehavior(dropdown).setClickMode(true));
	}
}
