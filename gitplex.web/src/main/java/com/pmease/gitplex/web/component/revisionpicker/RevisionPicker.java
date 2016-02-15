package com.pmease.gitplex.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.gitplex.core.model.DepotAndRevision;
import com.pmease.gitplex.core.model.Depot;

@SuppressWarnings("serial")
public abstract class RevisionPicker extends DropdownLink {

	private final IModel<Depot> repoModel;
	
	private String revision;
	
	private final boolean canCreateRef;
	
	public RevisionPicker(String id, IModel<Depot> repoModel, String revision, boolean canCreateRef) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
		this.canCreateRef = canCreateRef;
	}
	
	public RevisionPicker(String id, IModel<Depot> repoModel, String revision) {
		this(id, repoModel, revision, false);
	}

	@Override
	protected Component newContent(String id) {
		return new RevisionSelector(id, repoModel, revision, canCreateRef) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				close(target);
				RevisionPicker.this.revision = revision;
				target.add(RevisionPicker.this);
				
				RevisionPicker.this.onSelect(target, revision);
			}

			@Override
			protected void onModalOpened(AjaxRequestTarget target, ModalPanel modal) {
				super.onModalOpened(target, modal);
				close(target);
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
		DepotAndRevision repoAndRevision = new DepotAndRevision(repoModel.getObject(), revision);
		String label = repoAndRevision.getBranch();
		if (label != null) {
			iconClass = "fa fa-ext fa-branch";
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
		repoModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String revision);
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
}
