package com.turbodev.server.web.component.branchpicker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class BranchPicker extends DropdownLink {

	private final IModel<Project> projectModel;
	
	private String branch;
	
	public BranchPicker(String id, IModel<Project> projectModel, String branch) {
		super(id);
		
		this.projectModel = projectModel;
		this.branch = branch;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new BranchSelector(id, projectModel, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				dropdown.close();
				BranchPicker.this.branch = branch;
				target.add(BranchPicker.this);
				
				BranchPicker.this.onSelect(target, branch);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(String.format("<i class='fa fa-code-fork'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", 
				HtmlEscape.escapeHtml5(branch)));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String branch);
}
