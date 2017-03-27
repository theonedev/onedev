package com.gitplex.server.web.component.branchpicker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class BranchPicker extends DropdownLink {

	private final IModel<Depot> depotModel;
	
	private String branch;
	
	public BranchPicker(String id, IModel<Depot> depotModel, String branch) {
		super(id);
		
		this.depotModel = depotModel;
		this.branch = branch;
	}

	@Override
	protected Component newContent(String id) {
		return new BranchSelector(id, depotModel, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				closeDropdown();
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
		depotModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String branch);
}
