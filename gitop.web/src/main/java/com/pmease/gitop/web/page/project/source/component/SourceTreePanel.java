package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class SourceTreePanel extends ProjectPanel {

	private final IModel<List<String>> pathModel;
	private final IModel<String> revisionModel;
	
	public SourceTreePanel(String id, 
			IModel<Project> project,
			IModel<String> revisionModel,
			IModel<List<String>> pathModel) {
		
		super(id, project);
		
		this.revisionModel = revisionModel;
		this.pathModel = pathModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer revSelector = new WebMarkupContainer("revselector");
		revSelector.setOutputMarkupId(true);
		add(revSelector);
		revSelector.add(new Label("rev", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return revisionModel.getObject();
			}
			
		}));
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", false) {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "dropdownfrag", SourceTreePanel.this);
				return frag;
			}
			
		};
		
		add(dropdown);
		
		revSelector.add(new DropdownBehavior(dropdown));
	}
	
	@Override
	protected void onDetach() {
		if (pathModel != null) {
			pathModel.detach();
		}
		
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		super.onDetach();
	}
}
