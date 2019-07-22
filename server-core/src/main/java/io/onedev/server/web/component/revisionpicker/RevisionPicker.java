package io.onedev.server.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectAndRevision;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class RevisionPicker extends DropdownLink {

	private final IModel<Project> projectModel;
	
	private String revision;
	
	private final boolean canCreateRef;
	
	public RevisionPicker(String id, IModel<Project> projectModel, String revision, boolean canCreateRef) {
		super(id);
		
		this.projectModel = projectModel;
		this.revision = revision;
		this.canCreateRef = canCreateRef;
	}
	
	public RevisionPicker(String id, IModel<Project> projectModel, String revision) {
		this(id, projectModel, revision, false);
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new RevisionSelector(id, projectModel, revision, canCreateRef) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				dropdown.close();
				RevisionPicker.this.revision = revision;
				target.add(RevisionPicker.this);
				
				RevisionPicker.this.onSelect(target, revision);
			}

			@Override
			protected void onModalOpened(AjaxRequestTarget target, ModalPanel modal) {
				super.onModalOpened(target, modal);
				dropdown.close();
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
		ProjectAndRevision repoAndRevision = new ProjectAndRevision(projectModel.getObject(), revision);
		String label = repoAndRevision.getBranch();
		if (label != null) {
			iconClass = "fa fa-code-fork";
		} else {
			label = repoAndRevision.getTag();
			if (label != null) {
				iconClass = "fa fa-tag";
			} else {
				label = revision;
				if (ObjectId.isId(label))
					label = GitUtils.abbreviateSHA(label);
				iconClass = "fa fa-ext fa-commit";
			}
		} 
		
		return Model.of(String.format("<i class='%s'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", iconClass, label));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String revision);
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
}
