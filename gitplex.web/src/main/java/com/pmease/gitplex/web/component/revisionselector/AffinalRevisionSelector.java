package com.pmease.gitplex.web.component.revisionselector;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public abstract class AffinalRevisionSelector extends Panel {

	private final IModel<Repository> repoModel;
	
	private String revision;
	
	public AffinalRevisionSelector(String id, IModel<Repository> repoModel, String initialRevision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = initialRevision;
	}
	
	private void newRevisionSelector(@Nullable AjaxRequestTarget target) {
		RevisionSelector revisionSelector = new RevisionSelector("revisionSelector", repoModel, revision) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				dropdownLink.add(AttributeAppender.append("class", "btn btn-default"));
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionSelector.this.revision = revision;
				newRevisionSelector(target);
				AffinalRevisionSelector.this.onSelect(target, revision);
			}

		};
		if (target != null) {
			replace(revisionSelector);
			target.add(revisionSelector);
		} else {
			add(revisionSelector);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AffinalRepositoryChoice("repositoryChoice", repoModel, repoModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				revision = repoModel.getObject().getDefaultBranch();
				newRevisionSelector(target);
				onSelect(target, revision);
			}

		}));
		
		newRevisionSelector(null);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, String revision);
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
