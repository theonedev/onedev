package com.pmease.gitplex.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.support.DepotAndRevision;

@SuppressWarnings("serial")
public abstract class RevisionPicker extends DropdownLink {

	private final IModel<Depot> depotModel;
	
	private String revision;
	
	private final RevisionMode mode;
	
	public RevisionPicker(String id, IModel<Depot> depotModel, String revision, RevisionMode mode) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;
		this.mode = mode;
	}
	
	public RevisionPicker(String id, IModel<Depot> depotModel, String revision) {
		this(id, depotModel, revision, RevisionMode.CAN_INPUT_REV);
	}

	@Override
	protected Component newContent(String id) {
		return new RevisionSelector(id, depotModel, revision, mode) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				close();
				RevisionPicker.this.revision = revision;
				target.add(RevisionPicker.this);
				
				RevisionPicker.this.onSelect(target, revision);
			}

			@Override
			protected void onModalOpened(AjaxRequestTarget target, ModalPanel modal) {
				super.onModalOpened(target, modal);
				close();
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
