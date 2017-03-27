package com.gitplex.server.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.DepotAndRevision;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class RevisionPicker extends DropdownLink {

	private final IModel<Depot> depotModel;
	
	private String revision;
	
	private final boolean canCreateRef;
	
	public RevisionPicker(String id, IModel<Depot> depotModel, String revision, boolean canCreateRef) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;
		this.canCreateRef = canCreateRef;
	}
	
	public RevisionPicker(String id, IModel<Depot> depotModel, String revision) {
		this(id, depotModel, revision, false);
	}

	@Override
	protected Component newContent(String id) {
		return new RevisionSelector(id, depotModel, revision, canCreateRef) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				closeDropdown();
				RevisionPicker.this.revision = revision;
				target.add(RevisionPicker.this);
				
				RevisionPicker.this.onSelect(target, revision);
			}

			@Override
			protected void onModalOpened(AjaxRequestTarget target, ModalPanel modal) {
				super.onModalOpened(target, modal);
				closeDropdown();
			}

			@Override
			protected String getRevisionUrl(String revision) {
				return RevisionPicker.this.getRevisionUrl(revision);
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
		String iconClass;
		DepotAndRevision repoAndRevision = new DepotAndRevision(depotModel.getObject(), revision);
		String label = repoAndRevision.getBranch();
		if (label != null) {
			iconClass = "fa fa-code-fork";
		} else {
			label = repoAndRevision.getTag();
			if (label != null) {
				iconClass = "fa fa-tag";
			} else {
				label = revision;
				if (GitUtils.isHash(label))
					label = GitUtils.abbreviateSHA(label);
				iconClass = "fa fa-ext fa-commit";
			}
		} 
		
		return Model.of(String.format("<i class='%s'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", iconClass, label));
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String revision);
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
}
