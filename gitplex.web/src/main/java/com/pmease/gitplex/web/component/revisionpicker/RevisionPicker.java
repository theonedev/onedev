package com.pmease.gitplex.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.Constants;
import org.unbescape.html.HtmlEscape;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class RevisionPicker extends DropdownLink {

	private final IModel<Repository> repoModel;
	
	private String revision;
	
	public RevisionPicker(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
	}

	@Override
	protected Component newContent(String id) {
		return new RevisionSelector(id, repoModel, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				close(target);
				RevisionPicker.this.revision = revision;
				target.add(RevisionPicker.this);
				
				RevisionPicker.this.onSelect(target, revision);
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
		if (repoModel.getObject().getRef(Constants.R_HEADS + revision) != null)
			iconClass = "fa fa-ext fa-branch";
		else if (repoModel.getObject().getRef(Constants.R_TAGS + revision) != null)
			iconClass = "fa fa-tag";
		else
			iconClass = "fa fa-ext fa-commit";
		String label;
		if (GitUtils.isHash(revision)) 				
			label = GitUtils.abbreviateSHA(revision);
		else
			label = HtmlEscape.escapeHtml5(revision);
		
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
